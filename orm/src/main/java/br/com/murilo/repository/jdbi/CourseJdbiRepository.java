package br.com.murilo.repository.jdbi;

import br.com.murilo.config.JdbiConfig;
import br.com.murilo.domain.Course;
import br.com.murilo.domain.CourseRecord;
import br.com.murilo.repository.ICourseRepository;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementação de {@link ICourseRepository} usando <b>JDBI 3</b> com SQL Object API.
 *
 * <h2>Filosofia SQL-first do JDBI</h2>
 * <p>JDBI inverte a abordagem do ORM: ao invés de gerar SQL a partir de objetos,
 * você escreve SQL explicitamente e o JDBI cuida do mapeamento para objetos Java.
 * Esta abordagem garante:
 * <ul>
 *   <li>Controle total sobre queries — sem SQL gerado "por mágica"</li>
 *   <li>Facilidade de otimização — você vê e ajusta exatamente o SQL executado</li>
 *   <li>Zero risco de N+1 queries — não há lazy loading</li>
 *   <li>Mapeamento natural para {@code record}s JDK 21 — imutabilidade como padrão</li>
 * </ul>
 *
 * <h2>Dois modos de uso do JDBI</h2>
 *
 * <h3>1. Fluent API (handle.createQuery(...))</h3>
 * <p>Imperativa, mais verbosa, total controle. Exemplo:
 * <pre>{@code
 *   jdbi.withHandle(h ->
 *       h.createQuery("SELECT * FROM TB_COURSE WHERE id = :id")
 *        .bind("id", 1L)
 *        .mapTo(CourseRecord.class)
 *        .findFirst()
 *   );
 * }</pre>
 *
 * <h3>2. SQL Object API ({@link CourseDao})</h3>
 * <p>Declarativa, menos boilerplate, interfaces anotadas. Este repositório delega
 * para o SQL Object gerado pelo JDBI a partir da interface {@link CourseDao}.
 *
 * <h2>Modelo de transação JDBI</h2>
 * <ul>
 *   <li>{@code jdbi.withHandle()} — sem transação explícita (auto-commit por operação)</li>
 *   <li>{@code jdbi.inTransaction()} — abre transação, commit no retorno normal,
 *       rollback em exceção</li>
 *   <li>{@code jdbi.useTransaction()} — como inTransaction mas sem valor de retorno</li>
 * </ul>
 *
 * <h2>Trade-offs vs JPA/Hibernate</h2>
 * <ul>
 *   <li><b>Pro:</b> SQL explícito; ideal para queries analíticas complexas e JOINs;
 *       zero overhead de bytecode weaving; mapeamento direto para records imutáveis</li>
 *   <li><b>Con:</b> Schema deve ser gerenciado separadamente (Flyway/Liquibase);
 *       sem lazy loading; sem cache de segundo nível nativo;
 *       mais boilerplate para queries simples de CRUD</li>
 * </ul>
 */
public class CourseJdbiRepository implements ICourseRepository {

    private static final Logger log = LoggerFactory.getLogger(CourseJdbiRepository.class);

    private final Jdbi jdbi;

    public CourseJdbiRepository() {
        this.jdbi = JdbiConfig.getJdbi();
    }

    /** Construtor para injeção de dependência (testes). */
    public CourseJdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Course create(Course course) {
        var id = jdbi.inTransaction(h -> {
            var dao = h.attach(CourseDao.class);
            long generatedId = dao.insert(course.getCode(), course.getName(), course.getDescription());
            log.debug("Course inserted via JDBI with generated id={}", generatedId);
            return generatedId;
        });
        /*
         * JDBI retorna o registro como imutável. Para compatibilidade com ICourseRepository,
         * convertemos o record de volta para entidade. Em sistemas reais, considere
         * retornar CourseRecord diretamente nos use cases que usam JDBI.
         */
        var record = new CourseRecord(id, course.getCode(), course.getName(), course.getDescription());
        return record.toEntity();
    }

    @Override
    public Optional<Course> findById(Long id) {
        return jdbi.withHandle(h ->
                h.attach(CourseDao.class)
                 .findById(id)
                 .map(CourseRecord::toEntity)
        );
    }

    @Override
    public Optional<Course> findByCode(String code) {
        return jdbi.withHandle(h ->
                h.attach(CourseDao.class)
                 .findByCode(code)
                 .map(CourseRecord::toEntity)
        );
    }

    @Override
    public List<Course> findAll() {
        return jdbi.withHandle(h ->
                h.attach(CourseDao.class)
                 .findAll()
                 .stream()
                 .map(CourseRecord::toEntity)
                 .toList()  // JDK 16+ — cria List imutável
        );
    }

    /**
     * Versão otimizada de findAll que retorna diretamente {@link CourseRecord}
     * sem conversão desnecessária Course → CourseRecord → Course.
     * <p>Use este método em APIs REST / read models para máxima eficiência.
     */
    @Override
    public List<CourseRecord> findAllAsRecords() {
        return jdbi.withHandle(h ->
                h.attach(CourseDao.class).findAll()
        );
    }

    @Override
    public Course update(Course course) {
        jdbi.inTransaction(h -> {
            var record = new CourseRecord(course.getId(), course.getCode(), course.getName(), course.getDescription());
            int affected = h.attach(CourseDao.class).update(record);
            if (affected == 0) {
                throw new IllegalArgumentException("Course not found with id: " + course.getId());
            }
            log.debug("Course updated via JDBI, rows affected={}", affected);
            return affected;
        });
        return course;
    }

    @Override
    public void deleteById(Long id) {
        jdbi.useTransaction(h -> {
            int affected = h.attach(CourseDao.class).deleteById(id);
            log.debug("Course deleted via JDBI, id={}, rows affected={}", id, affected);
        });
    }

    /**
     * Demonstração de <b>Fluent API</b> do JDBI — alternativa ao SQL Object.
     *
     * <p>A Fluent API é útil quando:
     * <ul>
     *   <li>A query é gerada dinamicamente (filtros opcionais, ORDER BY variável)</li>
     *   <li>Você precisa de {@code ResultIterator} para processar resultado em streaming</li>
     *   <li>Está experimentando queries antes de formalizar em SQL Object</li>
     * </ul>
     *
     * @param namePattern fragmento do nome para busca (LIKE %fragment%)
     * @return cursos cujo nome contém o fragmento
     */
    public List<CourseRecord> findByNameLike(String namePattern) {
        return jdbi.withHandle(handle -> {
            var sql = """
                    SELECT id, code, name, description
                    FROM TB_COURSE
                    WHERE LOWER(name) LIKE LOWER(:pattern)
                    ORDER BY name
                    """;

            return handle.createQuery(sql)
                         .bind("pattern", "%" + namePattern + "%")
                         .mapToBean(CourseRecord.class)  // alternativa: .map(new BeanMapper<>(CourseRecord.class))
                         .list();
        });
    }

    /**
     * Demonstração de <b>batch insert</b> via JDBI — alta performance para volumes grandes.
     *
     * @param records lista de CourseRecord a inserir
     * @return número total de linhas inseridas
     */
    public int batchInsert(List<CourseRecord> records) {
        var codes        = records.stream().map(CourseRecord::code).toList();
        var names        = records.stream().map(CourseRecord::name).toList();
        var descriptions = records.stream().map(CourseRecord::description).toList();

        var results = jdbi.inTransaction(h ->
                h.attach(CourseDao.class).batchInsert(codes, names, descriptions)
        );

        int total = 0;
        for (int r : results) total += r;
        log.info("Batch insert completed: {} courses inserted", total);
        return total;
    }
}
