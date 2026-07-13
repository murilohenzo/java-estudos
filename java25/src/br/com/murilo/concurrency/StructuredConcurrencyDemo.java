package br.com.murilo.concurrency;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;

/**
 * Demonstração de <b>Structured Concurrency</b> — JEP 505 (5ª Preview no Java 25).
 *
 * <p>API alinhada à JEP 505: {@code StructuredTaskScope.open()} e joiners.
 *
 * <pre>
 * javac --enable-preview --release 25 ...
 * java  --enable-preview -cp . br.com.murilo.concurrency.StructuredConcurrencyDemo
 * </pre>
 */
public class StructuredConcurrencyDemo {

    record Response(String user, int orderId) {}

    public static void main(String[] args) throws Exception {
        System.out.println("=== Structured Concurrency — Java 25 / JEP 505 (Preview) ===\n");

        System.out.println("--- open() default: all succeed or any fail ---");
        System.out.println(handle());

        System.out.println("\n--- Joiner.anySuccessfulResultOrThrow ---");
        System.out.println("primeiro sucesso = " + firstSuccessfulMirror());

        System.out.println("\nCaso de uso: fan-out I/O sem thread leak / cancelamento atrasado.");
    }

    /** Exemplo clássico da JEP: findUser + fetchOrder. */
    static Response handle() throws Exception {
        try (var scope = StructuredTaskScope.open()) {
            Subtask<String> user = scope.fork(() -> findUser());
            Subtask<Integer> order = scope.fork(() -> fetchOrder());
            scope.join(); // falha se qualquer subtarefa falhar; cancela as demais
            return new Response(user.get(), order.get());
        }
    }

    static String findUser() throws InterruptedException {
        Thread.sleep(80);
        return "murilo";
    }

    static int fetchOrder() throws InterruptedException {
        Thread.sleep(50);
        return 42;
    }

    /** Qualquer subtarefa que completar com sucesso encerra o escopo. */
    static String firstSuccessfulMirror() throws Exception {
        try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulResultOrThrow())) {
            scope.fork(() -> {
                Thread.sleep(200);
                return "lento";
            });
            scope.fork(() -> {
                Thread.sleep(40);
                return "rápido";
            });
            return scope.join();
        }
    }
}
