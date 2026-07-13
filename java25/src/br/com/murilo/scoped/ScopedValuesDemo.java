package br.com.murilo.scoped;

import java.util.concurrent.Executors;

/**
 * Demonstração de <b>Scoped Values</b> — JEP 506 (GA no Java 25).
 *
 * <p>Scoped values compartilham dados imutáveis com callees na mesma thread
 * e com child threads, com lifetime delimitado pelo escopo sintático
 * ({@code where(...).run/call}). São a alternativa moderna a {@link ThreadLocal}
 * quando o dado é contexto (user, tenant, correlation-id), não estado mutável.
 */
public class ScopedValuesDemo {

    static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();
    static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();

    public static void main(String[] args) throws Exception {
        System.out.println("=== Scoped Values — Java 25 ===\n");

        demonstrateBasicBinding();
        demonstrateNestedRebind();
        demonstrateWithVirtualThreads();
        demonstrateCallReturningValue();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Binding básico — where + run
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateBasicBinding() {
        System.out.println("--- 1. Binding básico ---");

        ScopedValue.where(CURRENT_USER, "murilo").run(() -> {
            System.out.println("Dentro do escopo: user=" + CURRENT_USER.get());
            audit("abriu sessão");
        });

        System.out.println("Fora do escopo bound? " + CURRENT_USER.isBound());
        System.out.println();
    }

    static void audit(String action) {
        // callee profundo — não recebe user como parâmetro
        System.out.println("  [audit] user=" + CURRENT_USER.get() + " action=" + action);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Rebind aninhado — shadowing no escopo interno
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateNestedRebind() {
        System.out.println("--- 2. Rebind aninhado ---");

        ScopedValue.where(CURRENT_USER, "admin").run(() -> {
            System.out.println("outer: " + CURRENT_USER.get());

            ScopedValue.where(CURRENT_USER, "impersonated-user").run(() ->
                    System.out.println("inner: " + CURRENT_USER.get()));

            System.out.println("outer de novo: " + CURRENT_USER.get());
        });
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Herança para Virtual Threads
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateWithVirtualThreads() throws InterruptedException {
        System.out.println("--- 3. Com Virtual Threads ---");

        ScopedValue.where(CURRENT_USER, "murilo")
                .where(TENANT_ID, "acme-corp")
                .run(() -> {
                    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                        for (int i = 1; i <= 3; i++) {
                            int taskId = i;
                            executor.submit(() -> {
                                System.out.println("  vt-" + taskId
                                        + " user=" + CURRENT_USER.get()
                                        + " tenant=" + TENANT_ID.get()
                                        + " isVirtual=" + Thread.currentThread().isVirtual());
                            });
                        }
                    }
                });
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. call — retorna valor do escopo
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateCallReturningValue() throws Exception {
        System.out.println("--- 4. where(...).call(...) ---");

        String greeting = ScopedValue.where(CURRENT_USER, "ana")
                .call(() -> "Olá, " + CURRENT_USER.get() + "!");

        System.out.println(greeting);
        System.out.println("orElse fora do escopo: " + CURRENT_USER.orElse("<anonymous>"));
        System.out.println();
    }
}
