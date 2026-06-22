package br.com.murilo.repository.jpa;

import br.com.murilo.config.PersistenceConfig;
import br.com.murilo.domain.Course;
import br.com.murilo.domain.CourseRecord;
import br.com.murilo.repository.ICourseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Implementação de {@link ICourseRepository} usando <b>Pure JPA</b>
 * ({@code jakarta.persistence.*}).
 *
 * <h2>Características da camada JPA Pura</h2>
 * <ul>
 *   <li><b>Portabilidade:</b> usa apenas API padronizada JPA 3.1 (Jakarta EE 10).
 *       Trocar Hibernate por EclipseLink ou OpenJPA não exige mudança no código.</li>
 *   <li><b>JPQL:</b> Java Persistence Query Language — sintaxe orientada a objetos,
 *       independente de banco de dados. O JPA provider traduz para SQL nativo.</li>
 *   <li><b>First-level Cache (L1):</b> cada {@code EntityManager} mantém um contexto
 *       de persistência que deduplica entidades pelo ID. Dentro de uma transação,
 *       dois {@code find()} com o mesmo id retornam o mesmo objeto em memória.</li>
 *   <li><b>Change Tracking:</b> entidades "managed" têm suas mudanças detectadas
 *       automaticamente no flush, sem necessidade de chamar update/merge manualmente.</li>
 * </ul>
 *
 * <h2>Padrão de transação usado</h2>
 * <p>Usamos {@code RESOURCE_LOCAL} (sem JTA/servidor de aplicação).
 * O padrão {@code executeInTransaction} encapsula o ciclo:
 * begin → execute → commit → rollback on error → close.
 *
 * <h2>Trade-offs vs Hibernate Nativo</h2>
 * <ul>
 *   <li><b>Pro:</b> Portável; bem documentado; compatível com Spring Data JPA</li>
 *   <li><b>Con:</b> Não acessa {@code StatelessSession}, filtros dinâmicos
 *       Hibernate, ou estratégias de batch avançadas sem unwrap</li>
 * </ul>
 *
 * <h2>Trade-offs vs JDBI</h2>
 * <ul>
 *   <li><b>Pro:</b> Menor boilerplate em CRUD; lazy loading automático; schema auto-gerenciado</li>
 *   <li><b>Con:</b> Risco de N+1 queries se collections não forem controladas;
 *       SQL gerado pode ser sub-ótimo em queries complexas</li>
 * </ul>
 */
public class CourseJpaRepository implements ICourseRepository {

    private static final Logger log = LoggerFactory.getLogger(CourseJpaRepository.class);

    @Override
    public Course create(Course course) {
        return executeInTransaction(em -> {
            em.persist(course);
            /*
             * flush() força o SQL INSERT imediato — útil quando precisamos
             * do ID gerado antes do commit. Sem flush, o INSERT pode ocorrer
             * em qualquer momento antes do commit (lazy write).
             */
            em.flush();
            log.debug("Course persisted: {}", course);
            return course;
        });
    }

    @Override
    public Optional<Course> findById(Long id) {
        return withEntityManager(em -> {
            /*
             * em.find() usa o L1 cache antes de ir ao banco.
             * Para garantir leitura fresh do banco, use:
             *   em.find(Course.class, id, LockModeType.PESSIMISTIC_READ)
             * ou evite a entidade managed usando uma projeção JPQL.
             */
            var course = em.find(Course.class, id);
            return Optional.ofNullable(course);
        });
    }

    @Override
    public Optional<Course> findByCode(String code) {
        return withEntityManager(em -> {
            /*
             * TypedQuery com parâmetro nomeado (:code) — evita SQL Injection
             * e permite reuso de query plan pelo JPA provider.
             * UPPER() para busca case-insensitive.
             */
            TypedQuery<Course> query = em.createQuery(
                    "SELECT c FROM Course c WHERE UPPER(c.code) = UPPER(:code)",
                    Course.class
            );
            query.setParameter("code", code);

            return query.getResultStream().findFirst();
        });
    }

    @Override
    public List<Course> findAll() {
        return withEntityManager(em -> {
            /*
             * "FROM Course c" equivale a "SELECT c FROM Course c".
             * JPQL usa o nome da CLASSE, não da tabela.
             */
            return em.createQuery("FROM Course c ORDER BY c.name ASC", Course.class)
                     .getResultList();
        });
    }

    @Override
    public List<CourseRecord> findAllAsRecords() {
        return withEntityManager(em -> {
            /*
             * Constructor Expression JPQL — projeta diretamente para CourseRecord.
             * Mais eficiente que carregar entidades completas quando só precisamos
             * de um subconjunto dos campos (evita overhead do change-tracking).
             */
            var jpql = """
                    SELECT new br.com.murilo.domain.CourseRecord(c.id, c.code, c.name, c.description)
                    FROM Course c
                    ORDER BY c.name ASC
                    """;
            return em.createQuery(jpql, CourseRecord.class).getResultList();
        });
    }

    @Override
    public Course update(Course course) {
        return executeInTransaction(em -> {
            /*
             * merge() copia o estado do objeto "detached" (course) para uma entidade
             * "managed" dentro do contexto de persistência atual.
             * Retorna a instância managed — use-a, não o objeto original.
             */
            var managed = em.merge(course);
            log.debug("Course merged: {}", managed);
            return managed;
        });
    }

    @Override
    public void deleteById(Long id) {
        executeInTransactionVoid(em -> {
            /*
             * getReference() retorna um proxy sem ir ao banco imediatamente.
             * remove() no proxy gera o DELETE sem necessitar do objeto completo.
             * Se o ID não existir, EntityNotFoundException é lançada ao flush.
             */
            var ref = em.getReference(Course.class, id);
            em.remove(ref);
            log.debug("Course with id={} deleted", id);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers — gerenciamento do ciclo de vida do EntityManager
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Executa uma operação READ em um EntityManager e fecha o contexto ao final.
     * Sem transação explícita — leituras JPA funcionam fora de transação (auto-commit JDBC).
     */
    private <T> T withEntityManager(Function<EntityManager, T> operation) {
        try (var em = PersistenceConfig.createEntityManager()) {
            return operation.apply(em);
        }
    }

    /**
     * Executa uma operação WRITE dentro de uma transação JPA.
     * Rollback automático em caso de exceção.
     */
    private <T> T executeInTransaction(Function<EntityManager, T> operation) {
        try (var em = PersistenceConfig.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                T result = operation.apply(em);
                tx.commit();
                return result;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                log.error("Transaction rolled back", e);
                throw e;
            }
        }
    }

    private void executeInTransactionVoid(Consumer<EntityManager> operation) {
        executeInTransaction(em -> { operation.accept(em); return null; });
    }
}
