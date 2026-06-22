package br.com.murilo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Provedor singleton de {@link Jdbi} com pool HikariCP para a camada SQL-first.
 *
 * <h2>O que é JDBI?</h2>
 * <p>JDBI (Java DataBase Interface) é uma biblioteca SQL <em>convenience wrapper</em>
 * sobre JDBC puro. Diferente de ORMs como Hibernate, o JDBI é <b>SQL-first</b>:
 * você escreve SQL explicitamente e ele cuida do mapeamento ResultSet ↔ objetos Java.
 *
 * <h2>Quando usar JDBI vs JPA/Hibernate?</h2>
 *
 * <table border="1" cellpadding="4">
 *   <caption>JDBI vs JPA — guia de decisão</caption>
 *   <tr>
 *     <th>Cenário</th>
 *     <th>Preferir</th>
 *     <th>Motivo</th>
 *   </tr>
 *   <tr>
 *     <td>CRUD simples com entidades mapeáveis</td>
 *     <td>JPA</td>
 *     <td>Menos boilerplate; schema auto-gerenciado</td>
 *   </tr>
 *   <tr>
 *     <td>Queries complexas com múltiplos JOINs</td>
 *     <td>JDBI</td>
 *     <td>SQL explícito; sem risco de N+1; fácil tuning</td>
 *   </tr>
 *   <tr>
 *     <td>Read models / projeções</td>
 *     <td>JDBI + Records</td>
 *     <td>Mapeia diretamente para records JDK 21; zero overhead</td>
 *   </tr>
 *   <tr>
 *     <td>Bulk insert/update (>1000 rows)</td>
 *     <td>JDBI ou Hibernate StatelessSession</td>
 *     <td>JPA entity manager tem overhead de L1 cache por entidade</td>
 *   </tr>
 *   <tr>
 *     <td>Microserviço leve sem Spring</td>
 *     <td>JDBI</td>
 *     <td>Jar menor; startup mais rápido; sem reflexão de entidades</td>
 *   </tr>
 *   <tr>
 *     <td>Domain model rico com relacionamentos</td>
 *     <td>JPA/Hibernate</td>
 *     <td>Lazy loading; cascade; L2 cache; JPQL portável</td>
 *   </tr>
 * </table>
 *
 * <h2>Arquitetura JDBI neste projeto</h2>
 * <pre>
 *   JdbiConfig ──► HikariDataSource ──► PostgreSQL
 *       │
 *       └──► Jdbi.create(dataSource)
 *               ├── SqlObjectPlugin  (permite interfaces @SqlQuery/@SqlUpdate)
 *               └── PostgresPlugin   (UUID, arrays, HStore, Instant, etc.)
 * </pre>
 *
 * <h2>HikariCP — por que é o melhor pool para JVM?</h2>
 * <p>Benchmarks independentes mostram HikariCP 2–10x mais rápido que c3p0,
 * DBCP e BoneCP em alta concorrência. Motivos:
 * <ul>
 *   <li>Bytecode gerado em runtime para evitar reflexão no hot path</li>
 *   <li>Lock-free queue com CAS para checkout de conexões</li>
 *   <li>Timeout de validação via TCP keepalive, não via SELECT 1</li>
 * </ul>
 */
public final class JdbiConfig {

    private static final Logger log = LoggerFactory.getLogger(JdbiConfig.class);

    private JdbiConfig() {}

    private static final class Holder {
        static final HikariDataSource DATA_SOURCE = createPool();
        static final Jdbi JDBI = createJdbi(DATA_SOURCE);

        private static HikariDataSource createPool() {
            log.info("Initializing HikariCP connection pool");
            var cfg = new HikariConfig();

            // ── Conexão ──────────────────────────────────────────────────
            cfg.setJdbcUrl("jdbc:postgresql://localhost:5432/vendas_online");
            cfg.setUsername("postgres");
            cfg.setPassword("admin");
            cfg.setDriverClassName("org.postgresql.Driver");

            // ── Pool sizing ────────────────────────────────────────────
            // Regra geral: pool_size = (num_cores * 2) + num_spindle_disks
            // Para PostgreSQL em dev local, 10 conexões são suficientes.
            cfg.setMaximumPoolSize(10);
            cfg.setMinimumIdle(2);

            // ── Timeouts ────────────────────────────────────────────────
            cfg.setConnectionTimeout(30_000);  // ms para obter conexão do pool
            cfg.setIdleTimeout(600_000);        // ms antes de remover conexão ociosa
            cfg.setMaxLifetime(1_800_000);      // 30 min — rotação de conexões longas

            // ── Validação ───────────────────────────────────────────────
            cfg.setConnectionTestQuery("SELECT 1");  // fallback se isValid() não funcionar
            cfg.setPoolName("HikariPool-mod31");

            // ── Auto-commit OFF: JDBI gerencia transações explicitamente ─
            cfg.setAutoCommit(false);

            return new HikariDataSource(cfg);
        }

        private static Jdbi createJdbi(DataSource ds) {
            log.info("Configuring JDBI with SqlObject and Postgres plugins");
            return Jdbi.create(ds)
                    .installPlugin(new SqlObjectPlugin())  // habilita @SqlQuery, @SqlUpdate, etc.
                    .installPlugin(new PostgresPlugin());  // suporte a UUID, arrays, Instant, etc.
        }
    }

    /** Retorna a instância {@link Jdbi} configurada com pool e plugins. */
    public static Jdbi getJdbi() {
        return Holder.JDBI;
    }

    /** Retorna o {@link DataSource} HikariCP. Útil para integração com outros frameworks. */
    public static DataSource getDataSource() {
        return Holder.DATA_SOURCE;
    }

    /** Libera todas as conexões do pool. Invoke no shutdown da aplicação. */
    public static void shutdown() {
        if (!Holder.DATA_SOURCE.isClosed()) {
            log.info("Closing HikariCP pool");
            Holder.DATA_SOURCE.close();
        }
    }
}
