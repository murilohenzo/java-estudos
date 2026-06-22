import java.util.HashMap;
import java.util.Map;

/**
 * <h1>Proxy — Padrão Estrutural</h1>
 *
 * <h2>Intenção</h2>
 * <p>Fornecer um substituto ou marcador de posição para outro objeto para controlar
 * o acesso a ele. O Proxy tem a mesma interface do objeto real — o cliente não percebe
 * a diferença.
 *
 * <h2>Tipos de Proxy</h2>
 * <ul>
 *   <li><b>Virtual Proxy</b> — adía criação cara (lazy initialization)</li>
 *   <li><b>Protection Proxy</b> — controla acesso com base em permissões</li>
 *   <li><b>Caching Proxy</b> — armazena resultados de operações custosas</li>
 *   <li><b>Remote Proxy</b> — representa objeto em outro espaço de endereço (RMI, REST)</li>
 *   <li><b>Logging Proxy</b> — registra chamadas sem modificar o objeto real</li>
 *   <li><b>Smart Reference</b> — executa ações adicionais ao acessar o objeto</li>
 * </ul>
 *
 * <h2>Diferença Proxy vs Decorator</h2>
 * <ul>
 *   <li><b>Proxy</b>: geralmente gerencia o ciclo de vida do objeto real; cliente não o escolhe</li>
 *   <li><b>Decorator</b>: composto pelo cliente; adiciona comportamento sem controle de acesso</li>
 * </ul>
 *
 * <h2>No JDK</h2>
 * <ul>
 *   <li>{@code java.lang.reflect.Proxy} — Dynamic Proxy para AOP</li>
 *   <li>Spring AOP: {@code @Transactional}, {@code @Cacheable} geram proxies dinamicamente</li>
 *   <li>JPA lazy loading — o Hibernate retorna proxies de entidades</li>
 * </ul>
 */
public class ProxyDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // SUBJECT — interface comum para Real e Proxy
    // ─────────────────────────────────────────────────────────────────────────

    interface CourseRepository {
        String findByCode(String code);
        void save(String code, String name);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REAL SUBJECT — operação cara (simulando banco de dados)
    // ─────────────────────────────────────────────────────────────────────────

    static class DatabaseCourseRepository implements CourseRepository {
        private final Map<String, String> db = new HashMap<>();
        private int queryCount = 0;

        public DatabaseCourseRepository() {
            // Seed de dados
            db.put("JAVA-01", "Java Fundamentos");
            db.put("JAVA-21", "Java 21 Modern Features");
            db.put("SQL-01",  "SQL e Banco de Dados");
        }

        @Override
        public String findByCode(String code) {
            queryCount++;
            simulateSlowQuery();
            System.out.printf("    [DB] query #%d → SELECT * WHERE code = '%s'%n", queryCount, code);
            return db.get(code);
        }

        @Override
        public void save(String code, String name) {
            queryCount++;
            simulateSlowQuery();
            System.out.printf("    [DB] query #%d → INSERT '%s' = '%s'%n", queryCount, code, name);
            db.put(code, name);
        }

        private void simulateSlowQuery() {
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        int queryCount() { return queryCount; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PROXY 1 — Caching Proxy
    // ─────────────────────────────────────────────────────────────────────────

    static class CachingCourseProxy implements CourseRepository {
        private final DatabaseCourseRepository real;
        private final Map<String, String> cache = new HashMap<>();
        private int cacheHits = 0;

        CachingCourseProxy(DatabaseCourseRepository real) { this.real = real; }

        @Override
        public String findByCode(String code) {
            if (cache.containsKey(code)) {
                cacheHits++;
                System.out.printf("    [CACHE HIT #%d] '%s'%n", cacheHits, code);
                return cache.get(code);
            }
            System.out.printf("    [CACHE MISS] '%s' → consultando DB...%n", code);
            var result = real.findByCode(code);
            if (result != null) cache.put(code, result);
            return result;
        }

        @Override
        public void save(String code, String name) {
            real.save(code, name);
            cache.put(code, name);  // invalida/atualiza cache
        }

        void stats() {
            System.out.printf("    Cache: %d hits | %d DB queries%n", cacheHits, real.queryCount());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PROXY 2 — Protection Proxy (controle de acesso)
    // ─────────────────────────────────────────────────────────────────────────

    enum Role { ADMIN, VIEWER }

    static class ProtectedCourseProxy implements CourseRepository {
        private final CourseRepository delegate;
        private final Role userRole;

        ProtectedCourseProxy(CourseRepository delegate, Role role) {
            this.delegate = delegate;
            this.userRole = role;
        }

        @Override
        public String findByCode(String code) {
            return delegate.findByCode(code);  // qualquer role pode ler
        }

        @Override
        public void save(String code, String name) {
            if (userRole != Role.ADMIN) {
                throw new SecurityException(
                    "Acesso negado: role '%s' não tem permissão de escrita".formatted(userRole));
            }
            delegate.save(code, name);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Proxy Pattern ===\n");

        // ── Caching Proxy ──
        System.out.println("--- Caching Proxy ---");
        var db    = new DatabaseCourseRepository();
        var proxy = new CachingCourseProxy(db);

        System.out.println("\n  1ª busca (miss):");
        System.out.println("  → " + proxy.findByCode("JAVA-21"));

        System.out.println("\n  2ª busca (hit, sem DB):");
        System.out.println("  → " + proxy.findByCode("JAVA-21"));

        System.out.println("\n  3ª busca diferente (miss):");
        System.out.println("  → " + proxy.findByCode("SQL-01"));

        System.out.println("\n  4ª busca (hit):");
        System.out.println("  → " + proxy.findByCode("JAVA-21"));

        System.out.println();
        proxy.stats();

        // ── Protection Proxy ──
        System.out.println("\n--- Protection Proxy ---");
        var adminProxy  = new ProtectedCourseProxy(proxy, Role.ADMIN);
        var viewerProxy = new ProtectedCourseProxy(proxy, Role.VIEWER);

        System.out.println("\n  ADMIN pode salvar:");
        adminProxy.save("JAVA-22", "Java 22 Features");

        System.out.println("\n  VIEWER não pode salvar:");
        try {
            viewerProxy.save("HACK", "Unauthorized");
        } catch (SecurityException e) {
            System.out.println("  SecurityException: " + e.getMessage());
        }

        System.out.println("\n  VIEWER pode ler:");
        System.out.println("  → " + viewerProxy.findByCode("JAVA-22"));
    }
}
