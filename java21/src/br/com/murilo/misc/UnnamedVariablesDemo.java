package br.com.murilo.misc;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstração de <b>Unnamed Variables e Unnamed Patterns</b> — JEP 443 (preview Java 21).
 *
 * <h2>O que são?</h2>
 * <p>Variáveis e padrões sem nome, representados pelo caractere {@code _} (underscore).
 * Indicam explicitamente ao leitor (e ao compilador) que o valor é <b>intencionalmente ignorado</b>.
 *
 * <h2>Benefício cognitivo</h2>
 * <p>Antes de JEP 443, para ignorar uma variável eram necessários nomes como
 * {@code dummy}, {@code ignored}, {@code unused}, {@code _unused} — cada um
 * com semântica ambígua. Com {@code _}, a intenção é inequívoca.
 *
 * <h2>Casos de uso</h2>
 * <ul>
 *   <li>Padrões em switch onde o binding não é usado</li>
 *   <li>Blocos catch onde a exceção não é inspecionada</li>
 *   <li>Loops enhanced-for onde o elemento é irrelevante</li>
 *   <li>try-with-resources onde o recurso não é referenciado explicitamente</li>
 * </ul>
 *
 * @see <a href="https://openjdk.org/jeps/443">JEP 443</a>
 */
public class UnnamedVariablesDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // Domínio auxiliar
    // ─────────────────────────────────────────────────────────────────────────

    sealed interface Event permits Event.Login, Event.Logout, Event.Purchase, Event.PageView {
        record Login(String user, String ip)          implements Event {}
        record Logout(String user)                    implements Event {}
        record Purchase(String user, double amount)   implements Event {}
        record PageView(String url, int durationMs)   implements Event {}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Unnamed Variables em switch
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Conta apenas eventos de compra; outros eventos são ignorados.
     *
     * <p>Com {@code _}, a intenção de ignorar o binding é explícita.
     * O compilador também garante que {@code _} não é usado acidentalmente.
     */
    static long countPurchases(List<Event> events) {
        int count = 0;
        for (var event : events) {
            // _ indica: "combina com este tipo, mas não preciso do binding"
            switch (event) {
                case Event.Purchase(var user, var amount) -> count++;
                // Unnamed pattern _ cobre todos os outros casos
                case Event.Login _, Event.Logout _, Event.PageView _ -> {}
            }
        }
        return count;
    }

    /**
     * Soma o total de compras — usa o binding {@code amount} mas ignora {@code user}.
     */
    static double totalRevenue(List<Event> events) {
        double total = 0;
        for (var event : events) {
            if (event instanceof Event.Purchase(var _, var amount)) {
                total += amount;  // _ indica: "user não é necessário aqui"
            }
        }
        return total;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Unnamed Variables em catch
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Quando o bloco catch não inspeciona a exceção mas precisa do bloco
     * por efeitos colaterais ou retorno de fallback.
     */
    static int parseIntOrDefault(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException _) {
            // _ deixa claro que a exceção não carrega informação útil aqui
            return fallback;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Unnamed Variables em loops (contador, side effects)
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateLoops() {
        System.out.println("--- 3. Loops com _ ---");

        // Contar execuções de uma operação N vezes sem o índice
        var counter = new AtomicInteger();
        for (var _ : List.of(1, 2, 3, 4, 5)) {  // elemento ignorado
            counter.incrementAndGet();
        }
        System.out.println("Executed " + counter.get() + " times");

        // try-with-resources — recurso necessário para o fechamento, mas
        // o handle explícito não é usado no body
        // try (var _ = acquireLock()) { ... }  // futura sintaxe com Lock
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Múltiplos _ no mesmo escopo
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Diferente de uma variável normal, {@code _} pode ser declarado várias vezes
     * no mesmo escopo — cada ocorrência é independente.
     */
    static void demonstrateMultipleUnnamed() {
        System.out.println("--- 4. Múltiplos _ no mesmo escopo ---");

        var events = List.<Event>of(
                new Event.Login("ana", "192.168.1.1"),
                new Event.Purchase("bob", 99.90),
                new Event.PageView("/home", 3000),
                new Event.Purchase("ana", 250.00),
                new Event.Logout("ana")
        );

        // Cada case pode ter seu próprio _ independente
        for (var event : events) {
            var label = switch (event) {
                case Event.Login(var user, var _)      -> "login: " + user;
                case Event.Logout(var user)             -> "logout: " + user;
                case Event.Purchase(var user, var amt)  -> "purchase: " + user + " R$" + amt;
                case Event.PageView(var _, var ms)      -> "pageview: " + ms + "ms";
            };
            System.out.println("  " + label);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Unnamed Variables & Patterns — Java 21 ===\n");

        var events = List.<Event>of(
                new Event.Login("ana", "192.168.0.1"),
                new Event.Purchase("bob", 99.90),
                new Event.PageView("/home", 2500),
                new Event.Purchase("ana", 150.00),
                new Event.Logout("bob")
        );

        // 1. Contagem e soma
        System.out.println("--- 1 & 2. Contagem e Revenue ---");
        System.out.println("Purchases:    " + countPurchases(events));
        System.out.printf("Total revenue: R$%.2f%n%n", totalRevenue(events));

        // 2. Parse com fallback
        System.out.println("--- catch _ ---");
        System.out.println("parse '42'   → " + parseIntOrDefault("42", -1));
        System.out.println("parse 'abc'  → " + parseIntOrDefault("abc", -1));
        System.out.println();

        // 3. Loops
        demonstrateLoops();
        System.out.println();

        // 4. Múltiplos _
        demonstrateMultipleUnnamed();
    }
}
