package br.com.murilo.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provedor singleton de {@link EntityManagerFactory} para a camada JPA pura.
 *
 * <h2>Por que Singleton?</h2>
 * <p>{@code EntityManagerFactory} é um objeto <em>pesado</em>: seu bootstrap
 * envolve scanning de entidades, inicialização do connection pool interno
 * (quando configurado via {@code persistence.xml}), compilação de HQL/JPQL e
 * geração de proxies Hibernate. Criar uma nova instância por operação
 * (como no código original) causa:
 * <ul>
 *   <li>Overhead de ~50–200ms por operação (latência inaceitável em produção)</li>
 *   <li>Vazamento de conexões se {@code close()} não for invocado no finally</li>
 *   <li>Impossibilidade de aproveitar cache de segundo nível (L2 Cache)</li>
 * </ul>
 *
 * <h2>Initialization-on-demand Holder (Bill Pugh Singleton)</h2>
 * <p>Este padrão garante:
 * <ul>
 *   <li><b>Lazy initialization</b>: o EMF só é criado quando necessário</li>
 *   <li><b>Thread safety</b>: a JVM garante que a classe interna {@code Holder}
 *       é inicializada apenas uma vez pela ClassLoader, sem synchronized blocks</li>
 *   <li><b>Performance</b>: sem overhead de sincronização após a inicialização</li>
 * </ul>
 *
 * <h2>EntityManager vs EntityManagerFactory</h2>
 * <table border="1">
 *   <tr><th>EntityManagerFactory</th><th>EntityManager</th></tr>
 *   <tr><td>Thread-safe</td><td>NÃO thread-safe</td></tr>
 *   <tr><td>1 instância por aplicação</td><td>1 instância por thread/request</td></tr>
 *   <tr><td>Ciclo de vida: aplicação</td><td>Ciclo de vida: transação</td></tr>
 * </table>
 */
public final class PersistenceConfig {

    private static final Logger log = LoggerFactory.getLogger(PersistenceConfig.class);
    private static final String PERSISTENCE_UNIT = "ExemploJPA";

    private PersistenceConfig() {}

    private static final class Holder {
        static final EntityManagerFactory INSTANCE = createFactory();

        private static EntityManagerFactory createFactory() {
            log.info("Bootstrapping EntityManagerFactory for persistence unit '{}'", PERSISTENCE_UNIT);
            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
    }

    /**
     * Retorna a instância singleton do {@link EntityManagerFactory}.
     * Thread-safe sem overhead de sincronização (Bill Pugh pattern).
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return Holder.INSTANCE;
    }

    /**
     * Abre um novo {@link EntityManager}. <b>Deve ser fechado pelo chamador</b>
     * após o uso (try-with-resources ou bloco finally).
     *
     * <p>Cada {@code EntityManager} representa um contexto de persistência isolado
     * (unit of work). Jamais compartilhe entre threads ou requests HTTP.
     */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Encerra o {@link EntityManagerFactory} liberando todas as conexões do pool.
     * Deve ser invocado no shutdown da aplicação (ex: ShutdownHook, @PreDestroy).
     */
    public static void shutdown() {
        if (Holder.INSTANCE.isOpen()) {
            log.info("Closing EntityManagerFactory");
            Holder.INSTANCE.close();
        }
    }
}
