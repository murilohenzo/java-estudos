package br.com.murilo.config;

import br.com.murilo.domain.Course;
import br.com.murilo.domain.Registration;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provedor singleton de {@link SessionFactory} para uso com a API nativa Hibernate.
 *
 * <h2>JPA vs Hibernate Nativo — Quando usar cada um?</h2>
 *
 * <table border="1" cellpadding="4">
 *   <caption>Comparativo JPA x Hibernate API</caption>
 *   <tr>
 *     <th>Aspecto</th>
 *     <th>JPA ({@code EntityManager})</th>
 *     <th>Hibernate Nativo ({@code Session})</th>
 *   </tr>
 *   <tr>
 *     <td>API</td>
 *     <td>{@code jakarta.persistence.*}</td>
 *     <td>{@code org.hibernate.*}</td>
 *   </tr>
 *   <tr>
 *     <td>Portabilidade</td>
 *     <td>Alta — troca de ORM sem mudar código</td>
 *     <td>Baixa — acoplado ao Hibernate</td>
 *   </tr>
 *   <tr>
 *     <td>Recursos exclusivos</td>
 *     <td>—</td>
 *     <td>{@code StatelessSession}, {@code MultiTableBulkIdStrategy},
 *         fetch profiles, filtros dinâmicos</td>
 *   </tr>
 *   <tr>
 *     <td>Batch processing</td>
 *     <td>Limitado</td>
 *     <td>{@code StatelessSession} — zero overhead de 1st-level cache em batch</td>
 *   </tr>
 *   <tr>
 *     <td>Cache L2</td>
 *     <td>Sim (via JPA standard)</td>
 *     <td>Sim + regiões explícitas, estratégias por entidade</td>
 *   </tr>
 * </table>
 *
 * <h2>SessionFactory vs EntityManagerFactory</h2>
 * <p>Internamente, {@code EntityManagerFactory} do Hibernate <em>é</em> uma
 * {@code SessionFactory}. Você pode obter uma via:
 * <pre>{@code
 *   SessionFactory sf = emf.unwrap(SessionFactory.class);
 * }</pre>
 *
 * <h2>Configuração programática (sem persistence.xml)</h2>
 * <p>Esta classe configura o Hibernate via {@link Configuration} programaticamente,
 * sem depender do {@code persistence.xml}. Útil para testes de unidade, ferramentas
 * de migração e ambientes onde o XML não é prático.
 */
public final class HibernateConfig {

    private static final Logger log = LoggerFactory.getLogger(HibernateConfig.class);

    private HibernateConfig() {}

    private static final class Holder {
        static final SessionFactory INSTANCE = buildSessionFactory();

        private static SessionFactory buildSessionFactory() {
            log.info("Bootstrapping Hibernate SessionFactory (native API)");
            try {
                var config = new Configuration();

                // ── Conexão JDBC ──────────────────────────────────────────
                config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
                config.setProperty("hibernate.connection.url",          "jdbc:postgresql://localhost:5432/vendas_online");
                config.setProperty("hibernate.connection.username",     "postgres");
                config.setProperty("hibernate.connection.password",     "admin");

                // ── HikariCP como pool nativo do Hibernate ────────────────
                // hibernate.connection.provider_class configura o pool interno.
                // Em produção, prefira injetar DataSource externo via JNDI ou IoC.
                config.setProperty("hibernate.connection.pool_size", "10");

                // ── Dialeto e DDL ─────────────────────────────────────────
                config.setProperty("hibernate.dialect",        "org.hibernate.dialect.PostgreSQLDialect");
                config.setProperty("hibernate.hbm2ddl.auto",  "update");

                // ── Logging SQL ───────────────────────────────────────────
                config.setProperty("hibernate.show_sql",   "true");
                config.setProperty("hibernate.format_sql", "true");

                // ── Batch processing ──────────────────────────────────────
                // order_inserts/updates: reordena statements para maximizar batching por tipo.
                config.setProperty("hibernate.jdbc.batch_size",   "25");
                config.setProperty("hibernate.order_inserts",     "true");
                config.setProperty("hibernate.order_updates",     "true");

                // ── Entidades mapeadas ────────────────────────────────────
                config.addAnnotatedClass(Course.class);
                config.addAnnotatedClass(Registration.class);

                return config.buildSessionFactory();

            } catch (Exception e) {
                log.error("Failed to build SessionFactory", e);
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    /** Retorna a {@link SessionFactory} singleton. Thread-safe (Bill Pugh pattern). */
    public static SessionFactory getSessionFactory() {
        return Holder.INSTANCE;
    }

    /** Encerra a SessionFactory. Invoke no shutdown da aplicação. */
    public static void shutdown() {
        if (!Holder.INSTANCE.isClosed()) {
            log.info("Closing Hibernate SessionFactory");
            Holder.INSTANCE.close();
        }
    }
}
