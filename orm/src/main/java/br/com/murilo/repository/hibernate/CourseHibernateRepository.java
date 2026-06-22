package br.com.murilo.repository.hibernate;

import br.com.murilo.config.HibernateConfig;
import br.com.murilo.domain.Course;
import br.com.murilo.domain.CourseRecord;
import br.com.murilo.repository.ICourseRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Implementação de {@link ICourseRepository} usando a <b>API nativa do Hibernate</b>
 * ({@code org.hibernate.Session}).
 *
 * <h2>Por que usar a Session API ao invés do EntityManager JPA?</h2>
 *
 * <p>O {@link Session} do Hibernate expõe funcionalidades que não existem na spec JPA:
 *
 * <ul>
 *   <li><b>{@code StatelessSession}:</b> contexto de persistência sem L1 cache.
 *       Ideal para batch processing de centenas de milhares de registros sem
 *       OutOfMemoryError. Ver {@link #bulkInsert(List)}.</li>
 *   <li><b>Filtros dinâmicos ({@code @Filter}):</b> adiciona cláusulas WHERE
 *       automaticamente a todas as queries de uma entidade (ex: soft delete por tenant).</li>
 *   <li><b>HQL avançado:</b> funções proprietárias Hibernate como {@code TREAT()},
 *       {@code ELEMENTS()}, {@code INDICES()} e funções de banco via {@code function()}.</li>
 *   <li><b>Multi-Table Bulk Mutation:</b> {@code MERGE INTO} SQL nativo via Hibernate 6+.</li>
 *   <li><b>Session Events:</b> hooks para interceptar flush, dirty checking, etc.</li>
 * </ul>
 *
 * <h2>Session vs EntityManager</h2>
 * <p>Internamente, {@code EntityManager} do Hibernate <em>é</em> um {@code Session}.
 * A conversão é possível via:
 * <pre>{@code
 *   Session session = entityManager.unwrap(Session.class);
 * }</pre>
 *
 * <h2>HQL (Hibernate Query Language) vs JPQL</h2>
 * <table border="1" cellpadding="4">
 *   <caption>HQL x JPQL</caption>
 *   <tr><th>Aspecto</th><th>JPQL (JPA)</th><th>HQL (Hibernate)</th></tr>
 *   <tr><td>Portabilidade</td><td>Alta</td><td>Baixa (Hibernate-only)</td></tr>
 *   <tr><td>Funções extras</td><td>Básicas</td><td>TREAT, ELEMENTS, db functions</td></tr>
 *   <tr><td>Native SQL</td><td>createNativeQuery</td><td>createNativeMutationQuery</td></tr>
 *   <tr><td>Batch HQL</td><td>Limitado</td><td>UPDATE/DELETE sem carregar entidades</td></tr>
 * </table>
 */
public class CourseHibernateRepository implements ICourseRepository {

    private static final Logger log = LoggerFactory.getLogger(CourseHibernateRepository.class);

    @Override
    public Course create(Course course) {
        return executeInTransaction(session -> {
            /*
             * session.persist() — equivalente ao JPA em.persist().
             * session.save() é deprecated no Hibernate 6 — use persist().
             */
            session.persist(course);
            log.debug("Course persisted via Hibernate Session: {}", course);
            return course;
        });
    }

    @Override
    public Optional<Course> findById(Long id) {
        return withSession(session -> {
            /*
             * session.get() — vai ao banco se não estiver no L1 cache.
             * session.load() — retorna proxy sem ir ao banco imediatamente (lazy);
             *   lança ObjectNotFoundException se não existir ao acessar qualquer campo.
             * Prefira get() quando incerto sobre existência.
             */
            return Optional.ofNullable(session.get(Course.class, id));
        });
    }

    @Override
    public Optional<Course> findByCode(String code) {
        return withSession(session -> {
            /*
             * HQL com TypedQuery — mesma sintaxe do JPQL, mas via Session.
             * Query<Course> é a versão Hibernate de TypedQuery<Course>.
             */
            Query<Course> query = session.createQuery(
                    "FROM Course c WHERE UPPER(c.code) = UPPER(:code)",
                    Course.class
            );
            query.setParameter("code", code);
            return query.uniqueResultOptional();
        });
    }

    @Override
    public List<Course> findAll() {
        return withSession(session ->
            session.createQuery("FROM Course c ORDER BY c.name", Course.class)
                   .list()
        );
    }

    @Override
    public List<CourseRecord> findAllAsRecords() {
        return withSession(session -> {
            /*
             * Constructor expression HQL — projeta para CourseRecord diretamente.
             * Evita instanciar proxies e o overhead do change-tracking.
             */
            var hql = """
                    SELECT new br.com.murilo.domain.CourseRecord(c.id, c.code, c.name, c.description)
                    FROM Course c
                    ORDER BY c.name
                    """;
            return session.createQuery(hql, CourseRecord.class).list();
        });
    }

    @Override
    public Course update(Course course) {
        return executeInTransaction(session -> {
            /*
             * session.merge() — equivalente ao JPA em.merge().
             * session.update() está deprecated no Hibernate 6.
             * session.saveOrUpdate() também deprecated — use persist() + merge() conforme contexto.
             */
            return session.merge(course);
        });
    }

    @Override
    public void deleteById(Long id) {
        executeInTransactionVoid(session -> {
            /*
             * HQL DELETE — remove sem carregar a entidade em memória.
             * Muito mais eficiente que:
             *   Course c = session.get(Course.class, id); session.remove(c);
             * pois não executa SELECT antes do DELETE.
             *
             * Desvantagem: não dispara @PreRemove e não propaga cascades JPA.
             * Use HQL DELETE para deleções massivas; session.remove(entity) para
             * deleções unitárias que precisam de callbacks.
             */
            var deleted = session.createMutationQuery("DELETE FROM Course WHERE id = :id")
                                 .setParameter("id", id)
                                 .executeUpdate();
            log.debug("Deleted {} course(s) with id={}", deleted, id);
        });
    }

    /**
     * Demonstração de <b>StatelessSession</b> para bulk insert sem overhead de L1 cache.
     *
     * <p>{@code StatelessSession} difere de {@code Session} em aspectos críticos:
     * <ul>
     *   <li>Sem L1 cache — cada operação vai diretamente ao banco</li>
     *   <li>Sem change tracking — dirty checking desabilitado</li>
     *   <li>Sem cascades automáticos — você controla cada operação</li>
     *   <li>Sem events (@PrePersist, @PostLoad, etc.) — apenas operações JDBC puras</li>
     *   <li>Ideal para ETL, migrations e imports de grandes volumes</li>
     * </ul>
     *
     * @param courses lista de cursos a inserir em batch
     */
    public void bulkInsert(List<Course> courses) {
        try (var stateless = HibernateConfig.getSessionFactory().openStatelessSession()) {
            var tx = stateless.beginTransaction();
            try {
                for (var course : courses) {
                    stateless.insert(course);
                }
                tx.commit();
                log.info("Bulk inserted {} courses via StatelessSession", courses.size());
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    /**
     * Demonstração de <b>HQL Bulk Update</b> sem carregar entidades.
     *
     * <p>Atualiza múltiplas entidades com uma única instrução SQL UPDATE,
     * sem carregar nenhuma delas em memória. Útil para operações em massa.
     *
     * @param oldCode   código atual
     * @param newPrefix novo prefixo a concatenar
     * @return número de linhas afetadas
     */
    public int bulkUpdatePrefix(String oldCode, String newPrefix) {
        return executeInTransaction(session -> {
            return session.createMutationQuery(
                    "UPDATE Course SET name = CONCAT(:prefix, name) WHERE code = :code"
            )
            .setParameter("prefix", newPrefix)
            .setParameter("code", oldCode)
            .executeUpdate();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers — gerenciamento do ciclo de vida da Session
    // ─────────────────────────────────────────────────────────────────────────

    private <T> T withSession(Function<Session, T> operation) {
        try (var session = HibernateConfig.getSessionFactory().openSession()) {
            return operation.apply(session);
        }
    }

    private <T> T executeInTransaction(Function<Session, T> operation) {
        try (var session = HibernateConfig.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T result = operation.apply(session);
                tx.commit();
                return result;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                log.error("Hibernate transaction rolled back", e);
                throw e;
            }
        }
    }

    private void executeInTransactionVoid(Consumer<Session> operation) {
        executeInTransaction(session -> { operation.accept(session); return null; });
    }
}
