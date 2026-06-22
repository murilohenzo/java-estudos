import java.util.Comparator;
import java.util.List;

/**
 * <h1>Strategy — Padrão Comportamental</h1>
 *
 * <h2>Intenção</h2>
 * <p>Definir uma família de algoritmos, encapsular cada um e torná-los intercambiáveis.
 * Strategy permite que o algoritmo varie independentemente dos clientes que o utilizam.
 *
 * <h2>Componentes</h2>
 * <ul>
 *   <li><b>Strategy</b> — interface comum para todos os algoritmos</li>
 *   <li><b>ConcreteStrategy</b> — implementação específica do algoritmo</li>
 *   <li><b>Context</b> — usa o Strategy via composição; pode trocar a estratégia em runtime</li>
 * </ul>
 *
 * <h2>Quando usar</h2>
 * <ul>
 *   <li>Múltiplos algoritmos intercambiáveis: ordenação, desconto, roteamento, validação</li>
 *   <li>Quando if/else ou switch sobre tipo de algoritmo cresce demais</li>
 *   <li>Isolar variações do Open/Closed Principle</li>
 * </ul>
 *
 * <h2>Diferença Strategy vs Template Method</h2>
 * <ul>
 *   <li><b>Strategy:</b> composição — troca o objeto inteiro do algoritmo</li>
 *   <li><b>Template Method:</b> herança — sobrescreve partes do algoritmo na subclasse</li>
 * </ul>
 *
 * <h2>Java 21</h2>
 * <p>Em Java, interfaces funcionais + lambdas tornam o Strategy extremamente conciso
 * — muitas vezes não é necessário criar classes concretas explícitas.
 */
public class StrategyDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // DOMÍNIO
    // ─────────────────────────────────────────────────────────────────────────

    record Order(String id, String customerType, double subtotal, int itemCount) {}

    // ─────────────────────────────────────────────────────────────────────────
    // STRATEGY 1 — Política de Desconto
    // ─────────────────────────────────────────────────────────────────────────

    @FunctionalInterface
    interface DiscountStrategy {
        double apply(Order order);

        default String name() { return getClass().getSimpleName(); }
    }

    // Estratégias como records (Java 21) — encapsulam parâmetros claramente
    record NoDiscount() implements DiscountStrategy {
        public double apply(Order o) { return 0.0; }
        public String name() { return "Sem desconto"; }
    }

    record PercentageDiscount(double rate) implements DiscountStrategy {
        public double apply(Order o) { return o.subtotal() * rate; }
        public String name() { return "%.0f%% flat".formatted(rate * 100); }
    }

    record VolumeDiscount(int minItems, double rate) implements DiscountStrategy {
        public double apply(Order o) {
            return o.itemCount() >= minItems ? o.subtotal() * rate : 0.0;
        }
        public String name() { return "Volume (≥%d itens → %.0f%%)".formatted(minItems, rate * 100); }
    }

    record LoyaltyDiscount(String targetCustomerType, double rate) implements DiscountStrategy {
        public double apply(Order o) {
            return o.customerType().equals(targetCustomerType) ? o.subtotal() * rate : 0.0;
        }
        public String name() { return "Fidelidade %s (%.0f%%)".formatted(targetCustomerType, rate * 100); }
    }

    /** Estratégia composta — aplica múltiplas e retorna a maior */
    record BestOfStrategy(List<DiscountStrategy> strategies) implements DiscountStrategy {
        public double apply(Order o) {
            return strategies.stream()
                    .mapToDouble(s -> s.apply(o))
                    .max()
                    .orElse(0.0);
        }
        public String name() { return "Melhor de " + strategies.size(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONTEXT — Order Processor com estratégia injetada
    // ─────────────────────────────────────────────────────────────────────────

    static class OrderProcessor {
        private DiscountStrategy discountStrategy;

        OrderProcessor(DiscountStrategy strategy) {
            this.discountStrategy = strategy;
        }

        /** Permite trocar a estratégia em runtime. */
        void setDiscountStrategy(DiscountStrategy strategy) {
            this.discountStrategy = strategy;
        }

        double process(Order order) {
            double discount = discountStrategy.apply(order);
            double total    = order.subtotal() - discount;
            System.out.printf("  Order %-8s | subtotal R$%6.2f | desconto R$%5.2f | total R$%6.2f | estratégia: %s%n",
                    order.id(), order.subtotal(), discount, total, discountStrategy.name());
            return total;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STRATEGY 2 — Ordenação (Comparator É uma Strategy)
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateSortStrategies(List<Order> orders) {
        System.out.println("\n--- Strategy de Ordenação (Comparator = Strategy) ---");

        // Estratégias de ordenação como lambdas — conciso, sem classes extras
        Comparator<Order> bySubtotalDesc = Comparator.comparingDouble(Order::subtotal).reversed();
        Comparator<Order> byItemCount    = Comparator.comparingInt(Order::itemCount);
        Comparator<Order> byCustomer     = Comparator.comparing(Order::customerType)
                                               .thenComparingDouble(Order::subtotal);

        System.out.println("  Por valor (desc):    " +
                orders.stream().sorted(bySubtotalDesc).map(Order::id).toList());
        System.out.println("  Por itens (asc):     " +
                orders.stream().sorted(byItemCount).map(Order::id).toList());
        System.out.println("  Por tipo+valor:      " +
                orders.stream().sorted(byCustomer).map(Order::id).toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Strategy Pattern — Políticas de Desconto ===\n");

        var orders = List.of(
                new Order("ORD-001", "PREMIUM",  500.00, 10),
                new Order("ORD-002", "REGULAR",  150.00, 3),
                new Order("ORD-003", "PREMIUM",  80.00,  1),
                new Order("ORD-004", "REGULAR", 1200.00, 25)
        );

        // Estratégias definidas
        var strategies = List.<DiscountStrategy>of(
                new NoDiscount(),
                new PercentageDiscount(0.10),
                new VolumeDiscount(5, 0.15),
                new LoyaltyDiscount("PREMIUM", 0.20),
                new BestOfStrategy(List.of(
                        new PercentageDiscount(0.10),
                        new VolumeDiscount(5, 0.15),
                        new LoyaltyDiscount("PREMIUM", 0.20)
                ))
        );

        var processor = new OrderProcessor(new NoDiscount());

        for (var strategy : strategies) {
            System.out.println("--- Estratégia: " + strategy.name() + " ---");
            processor.setDiscountStrategy(strategy);  // troca em runtime
            for (var order : orders) {
                processor.process(order);
            }
            System.out.println();
        }

        // Estratégia como lambda (Java 8+ — o mais simples possível)
        System.out.println("--- Estratégia como lambda ---");
        processor.setDiscountStrategy(o -> o.subtotal() > 1000 ? o.subtotal() * 0.05 : 0.0);
        for (var order : orders) processor.process(order);

        demonstrateSortStrategies(orders);
    }
}
