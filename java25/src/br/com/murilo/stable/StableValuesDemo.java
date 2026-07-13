package br.com.murilo.stable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Demonstração de <b>Stable Values</b> — JEP 502 (Preview no Java 25).
 *
 * <p>Inicialização at-most-once, thread-safe, com potencial de constant-folding
 * pela JVM — padrão oficial {@code StableValue.of()} + {@code orElseSet}.
 *
 * <pre>
 * javac --enable-preview --release 25 ...
 * java  --enable-preview -cp . br.com.murilo.stable.StableValuesDemo
 * </pre>
 */
public class StableValuesDemo {

    record Logger(String name) {
        void info(String msg) {
            System.out.println("[" + name + "] " + msg);
        }

        static Logger create(Class<?> type) {
            System.out.println("  >> criando Logger para " + type.getSimpleName());
            return new Logger(type.getSimpleName());
        }
    }

    static final AtomicInteger initCount = new AtomicInteger();

    // Padrão JEP: campo StableValue + orElseSet sob demanda
    private final StableValue<Logger> logger = StableValue.of();

    // Stable supplier — declaração e factory juntas
    private final Supplier<Logger> auditLogger =
            StableValue.supplier(() -> {
                initCount.incrementAndGet();
                return Logger.create(StableValuesDemo.class);
            });

    Logger getLogger() {
        return logger.orElseSet(() -> Logger.create(StableValuesDemo.class));
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Stable Values — Java 25 / JEP 502 (Preview) ===\n");

        StableValuesDemo app = new StableValuesDemo();

        System.out.println("--- orElseSet (init lazy) ---");
        app.getLogger().info("primeira chamada");
        app.getLogger().info("segunda chamada (reusa a mesma instância)");

        System.out.println("\n--- StableValue.supplier sob concorrência ---");
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 8; i++) {
                pool.submit(() -> app.auditLogger.get().info("hello from " + Thread.currentThread()));
            }
        }
        System.out.println("initCount do supplier = " + initCount.get() + " (deve ser 1)");
        System.out.println("\nCaso de uso: recursos caros (logger, client, pool) sem eager init no startup.");
    }
}
