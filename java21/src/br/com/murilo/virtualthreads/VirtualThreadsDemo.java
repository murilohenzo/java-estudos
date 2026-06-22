package br.com.murilo.virtualthreads;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Demonstração de <b>Virtual Threads</b> — JEP 444 (GA no Java 21).
 *
 * <h2>O que são Virtual Threads?</h2>
 * <p>Virtual threads são threads <em>leves</em> gerenciadas pela JVM, não pelo SO.
 * Uma thread de plataforma (OS thread) consome ~1MB de stack; uma virtual thread
 * consome apenas ~few KB e pode existir em milhões simultaneamente.
 *
 * <h2>Modelo de execução</h2>
 * <pre>
 *   Virtual Thread 1 ─┐
 *   Virtual Thread 2 ─┤ → montadas em → Platform Thread (carrier)
 *   Virtual Thread 3 ─┘                 (pequeno pool de OS threads)
 *   Virtual Thread N ─┘
 * </pre>
 *
 * <p>Quando uma virtual thread bloqueia em I/O (JDBC, HTTP, arquivo), ela é
 * <b>desmontada</b> do carrier thread, que fica livre para executar outras
 * virtual threads. Ao I/O completar, a virtual thread é remontada em qualquer
 * carrier disponível.
 *
 * <h2>Quando usar Virtual Threads</h2>
 * <ul>
 *   <li><b>I/O bound:</b> servidores HTTP, chamadas JDBC, leitura de arquivos → ✅ ideal</li>
 *   <li><b>CPU bound:</b> cálculos pesados → ❌ não melhora (use ForkJoinPool)</li>
 *   <li><b>Alta concorrência:</b> 100k+ conexões simultâneas → ✅ ideal</li>
 * </ul>
 *
 * <h2>Impacto no design</h2>
 * <p>Virtual threads tornam o modelo <em>thread-per-request</em> viável novamente,
 * sem reactive programming. Código síncrono simples escala como código assíncrono.
 */
public class VirtualThreadsDemo {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("=== Virtual Threads — Java 21 ===\n");

        demonstrateCreation();
        demonstrateThroughput();
        demonstrateWithExecutor();
        demonstrateStructuredConcurrency();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Criação de Virtual Threads
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateCreation() throws InterruptedException {
        System.out.println("--- 1. Formas de criar Virtual Threads ---");

        // API 1: Thread.ofVirtual().start()
        Thread vt1 = Thread.ofVirtual()
                .name("vt-task-1")
                .start(() -> System.out.println("Virtual thread: " + Thread.currentThread()));

        // API 2: Thread.startVirtualThread() — atalho
        Thread vt2 = Thread.startVirtualThread(() ->
                System.out.println("Virtual thread 2 — isVirtual: " + Thread.currentThread().isVirtual()));

        // API 3: Thread.Builder para configuração
        Thread.Builder builder = Thread.ofVirtual().name("vt-worker-", 0);
        Thread vt3 = builder.start(() -> System.out.println("Builder virtual thread"));
        Thread vt4 = builder.start(() -> System.out.println("Builder virtual thread 2"));

        vt1.join(); vt2.join(); vt3.join(); vt4.join();
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Throughput: Platform vs Virtual Threads
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateThroughput() throws InterruptedException {
        int TASKS = 10_000;

        // Platform threads pool — limitado por OS (tipicamente ~few thousand)
        var start1 = Instant.now();
        try (var executor = Executors.newFixedThreadPool(200)) {
            var latch = new CountDownLatch(TASKS);
            for (int i = 0; i < TASKS; i++) {
                executor.submit(() -> {
                    simulateIoBoundWork();  // simula I/O (sleep)
                    latch.countDown();
                });
            }
            latch.await();
        }
        var platformTime = Duration.between(start1, Instant.now()).toMillis();

        // Virtual threads — sem limite prático, cada task tem sua própria thread
        var start2 = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var latch = new CountDownLatch(TASKS);
            for (int i = 0; i < TASKS; i++) {
                executor.submit(() -> {
                    simulateIoBoundWork();  // mesma simulação
                    latch.countDown();
                });
            }
            latch.await();
        }
        var virtualTime = Duration.between(start2, Instant.now()).toMillis();

        System.out.printf("--- 2. Throughput (%d tasks, ~10ms I/O each) ---%n", TASKS);
        System.out.printf("  Platform threads (pool=200): %d ms%n", platformTime);
        System.out.printf("  Virtual threads:             %d ms%n", virtualTime);
        System.out.printf("  Speedup: %.1fx%n%n", (double) platformTime / virtualTime);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. ExecutorService com Virtual Threads
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateWithExecutor() throws InterruptedException, ExecutionException {
        System.out.println("--- 3. ExecutorService com Virtual Threads ---");

        // newVirtualThreadPerTaskExecutor — 1 virtual thread por submitted task
        // Nenhuma fila de espera: tarefas executam imediatamente
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {

            var futures = new ArrayList<Future<String>>();

            for (int i = 1; i <= 5; i++) {
                int taskId = i;
                futures.add(exec.submit(() -> {
                    simulateIoBoundWork();
                    return "Task %d done by: %s".formatted(taskId, Thread.currentThread());
                }));
            }

            for (var f : futures) {
                System.out.println("  " + f.get());
            }
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Structured Concurrency — JEP 453 (preview no Java 21, GA no Java 23)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Structured Concurrency garante que threads criadas em um escopo
     * são finalizadas quando o escopo termina — sem "orphan threads".
     *
     * <p>Princípio: a lifetime de subtasks está contida na lifetime do pai.
     * Isso torna o código concurrent mais fácil de raciocinar e debugar.
     */
    static void demonstrateStructuredConcurrency() throws InterruptedException, ExecutionException {
        System.out.println("--- 4. Structured Concurrency (preview) ---");

        /*
         * Sem StructuredTaskScope (disponível como preview):
         * Usamos CompletableFuture para demonstrar o conceito
         */
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // Executa duas subtarefas "irmãs" — ambas precisam completar
            var f1 = executor.submit(() -> fetchUserData(1L));
            var f2 = executor.submit(() -> fetchOrderData(1L));

            // Ambas completam antes de continuar
            var user  = f1.get();
            var order = f2.get();

            System.out.println("  User: "  + user);
            System.out.println("  Order: " + order);
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitários
    // ─────────────────────────────────────────────────────────────────────────

    private static void simulateIoBoundWork() {
        try { Thread.sleep(10); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static String fetchUserData(long userId) {
        simulateIoBoundWork();
        return "User{id=" + userId + ", name='Ana'}";
    }

    private static String fetchOrderData(long orderId) {
        simulateIoBoundWork();
        return "Order{id=" + orderId + ", total=150.00}";
    }
}
