/**
 * <h1>State — Padrão Comportamental</h1>
 *
 * <h2>Intenção</h2>
 * <p>Permite que um objeto altere seu comportamento quando seu estado interno muda.
 * O objeto parecerá ter mudado sua classe.
 *
 * <h2>Problema</h2>
 * <p>Sem State, um objeto com comportamento variável por estado acumula grandes
 * blocos {@code if/else} ou {@code switch} em cada método, verificando o estado atual.
 * Cada nova transição exige modificar múltiplos métodos — viola Open/Closed.
 *
 * <h2>Diferença State vs Strategy</h2>
 * <ul>
 *   <li><b>State</b>: estados sabem uns dos outros e controlam as transições</li>
 *   <li><b>Strategy</b>: estratégias são independentes; o contexto escolhe qual usar</li>
 * </ul>
 *
 * <h2>Quando usar</h2>
 * <ul>
 *   <li>Máquinas de estado: pedido, conexão, semáforo, autômato finito</li>
 *   <li>Quando o comportamento depende do estado e muda em runtime</li>
 *   <li>Quando classes têm grandes condicionais que dependem de campos de estado</li>
 * </ul>
 *
 * <h2>Java 21</h2>
 * <p>Sealed interface + records para os estados — exaustividade garantida em compile time.
 * Pattern matching no switch para transições expressivas sem casting.
 */
public class StateDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // STATE — estados de um Pedido (sealed = todos os estados são conhecidos)
    // ─────────────────────────────────────────────────────────────────────────

    sealed interface OrderState
            permits OrderState.Draft, OrderState.Pending, OrderState.Paid,
                    OrderState.Shipped, OrderState.Delivered, OrderState.Cancelled {

        String label();

        // Transitions — cada estado define quais são permitidas
        record Draft()     implements OrderState { public String label() { return "Rascunho"; } }
        record Pending()   implements OrderState { public String label() { return "Aguardando Pagamento"; } }
        record Paid()      implements OrderState { public String label() { return "Pago"; } }
        record Shipped()   implements OrderState { public String label() { return "Em Transporte"; } }
        record Delivered() implements OrderState { public String label() { return "Entregue"; } }
        record Cancelled() implements OrderState { public String label() { return "Cancelado"; } }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONTEXT — Order que delega comportamento ao estado atual
    // ─────────────────────────────────────────────────────────────────────────

    static class Order {
        private final String id;
        private OrderState state;

        Order(String id) {
            this.id    = id;
            this.state = new OrderState.Draft();
        }

        // ── Transições de estado ─────────────────────────────────────────────

        void submit() {
            state = switch (state) {
                case OrderState.Draft d     -> { System.out.println(prefix() + "submetido para pagamento"); yield new OrderState.Pending(); }
                default                     -> throw new IllegalStateException(invalidTransition("submit"));
            };
        }

        void pay() {
            state = switch (state) {
                case OrderState.Pending p   -> { System.out.println(prefix() + "pagamento confirmado"); yield new OrderState.Paid(); }
                default                     -> throw new IllegalStateException(invalidTransition("pay"));
            };
        }

        void ship() {
            state = switch (state) {
                case OrderState.Paid p      -> { System.out.println(prefix() + "enviado para entrega"); yield new OrderState.Shipped(); }
                default                     -> throw new IllegalStateException(invalidTransition("ship"));
            };
        }

        void deliver() {
            state = switch (state) {
                case OrderState.Shipped s   -> { System.out.println(prefix() + "entregue ao cliente"); yield new OrderState.Delivered(); }
                default                     -> throw new IllegalStateException(invalidTransition("deliver"));
            };
        }

        void cancel() {
            state = switch (state) {
                case OrderState.Draft _,
                     OrderState.Pending _   -> { System.out.println(prefix() + "cancelado"); yield new OrderState.Cancelled(); }
                case OrderState.Paid _      -> { System.out.println(prefix() + "cancelado (reembolso iniciado)"); yield new OrderState.Cancelled(); }
                default                     -> throw new IllegalStateException(invalidTransition("cancel"));
            };
        }

        // ── Comportamentos dependentes de estado ─────────────────────────────

        double calculateShippingCost() {
            return switch (state) {
                case OrderState.Draft _     -> 0.0;
                case OrderState.Pending _   -> 15.90;   // estimativa
                case OrderState.Paid _      -> 12.90;   // desconto por pagamento
                case OrderState.Shipped _   -> 0.0;     // já pago
                case OrderState.Delivered _ -> 0.0;
                case OrderState.Cancelled _ -> 0.0;
            };
        }

        boolean canBeModified() {
            return switch (state) {
                case OrderState.Draft _     -> true;
                default                     -> false;
            };
        }

        // ── Utilitários ──────────────────────────────────────────────────────

        OrderState state()  { return state; }
        String     status() { return "[%s] %s".formatted(id, state.label()); }

        private String prefix() { return "  Pedido %s: ".formatted(id); }
        private String invalidTransition(String action) {
            return "Transição '%s' inválida no estado '%s'".formatted(action, state.label());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== State Pattern — Order State Machine ===\n");

        // ── Fluxo feliz ──
        System.out.println("--- Fluxo completo ---");
        var order = new Order("ORD-001");
        System.out.println("  Estado inicial: " + order.status());
        System.out.println("  Pode modificar: " + order.canBeModified());
        System.out.println("  Frete estimado: R$" + order.calculateShippingCost());

        order.submit();
        System.out.println("  " + order.status() + " | frete: R$" + order.calculateShippingCost());

        order.pay();
        System.out.println("  " + order.status() + " | frete: R$" + order.calculateShippingCost());
        System.out.println("  Pode modificar: " + order.canBeModified());

        order.ship();
        System.out.println("  " + order.status());

        order.deliver();
        System.out.println("  " + order.status());

        // ── Transição inválida ──
        System.out.println("\n--- Transição inválida ---");
        try {
            order.cancel();  // já entregue — não pode cancelar
        } catch (IllegalStateException e) {
            System.out.println("  ✗ " + e.getMessage());
        }

        // ── Cancelamento a partir de Paid ──
        System.out.println("\n--- Cancelamento com reembolso ---");
        var order2 = new Order("ORD-002");
        order2.submit();
        order2.pay();
        order2.cancel();
        System.out.println("  " + order2.status());

        // ── Java 21: switch exaustivo sobre sealed ──
        System.out.println("\n--- Diagnóstico via pattern matching ---");
        var orders = new Order[]{ order, order2 };
        for (var o : orders) {
            var msg = switch (o.state()) {
                case OrderState.Draft _     -> "Aguardando submissão";
                case OrderState.Pending _   -> "Aguardando pagamento do cliente";
                case OrderState.Paid _      -> "Preparar para envio";
                case OrderState.Shipped _   -> "Em rota de entrega";
                case OrderState.Delivered _ -> "Concluído";
                case OrderState.Cancelled _ -> "Arquivar pedido";
            };
            System.out.println("  " + o.id() + " → Ação: " + msg);
        }
    }

    // helper para acessar id de Order fora de sua classe interna
    static { }
}
