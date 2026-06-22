/**
 * <h1>Bridge — Padrão Estrutural</h1>
 *
 * <h2>Intenção</h2>
 * <p>Separar uma abstração de sua implementação para que as duas possam variar
 * de forma independente. Em vez de usar herança para estender funcionalidade em
 * duas dimensões, usa-se composição — a abstração contém uma referência à implementação.
 *
 * <h2>Problema</h2>
 * <p>Imagine um sistema de notificações com 2 tipos (Urgente, Regular) e 3 canais
 * (Email, SMS, Push). Com herança simples, seriam necessárias 2×3 = 6 subclasses.
 * Adicionar um 4º canal exigiria mais 2 subclasses — combinação explode exponencialmente.
 *
 * <h2>Solução</h2>
 * <pre>
 *   Abstraction (Notification) ──── bridge (composição) ────▶ Implementor (Channel)
 *        │                                                          │
 *    UrgentNotification                                       EmailChannel
 *    RegularNotification                                      SmsChannel
 *                                                             PushChannel
 * </pre>
 *
 * <h2>Quando usar</h2>
 * <ul>
 *   <li>Quando você quer evitar ligação permanente entre abstração e implementação</li>
 *   <li>Quando abstração e implementação devem ser extensíveis por subclasses</li>
 *   <li>Quando mudanças na implementação não devem impactar o código cliente</li>
 *   <li>Quando a hierarquia de classes cresce em duas dimensões independentes</li>
 * </ul>
 *
 * <h2>Diferença Bridge vs Adapter</h2>
 * <ul>
 *   <li><b>Bridge:</b> projetado antecipadamente para permitir variação independente</li>
 *   <li><b>Adapter:</b> retroativo — adapta interfaces incompatíveis já existentes</li>
 * </ul>
 */
public class BridgeDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // IMPLEMENTOR — hierarquia de canais de envio (varia independentemente)
    // ─────────────────────────────────────────────────────────────────────────

    interface Channel {
        void send(String recipient, String message);
        String name();
    }

    record EmailChannel() implements Channel {
        public void send(String recipient, String message) {
            System.out.printf("  [EMAIL → %s] %s%n", recipient, message);
        }
        public String name() { return "Email"; }
    }

    record SmsChannel() implements Channel {
        public void send(String recipient, String message) {
            System.out.printf("  [SMS → %s] %s%n", recipient, message);
        }
        public String name() { return "SMS"; }
    }

    record PushChannel(String appId) implements Channel {
        public void send(String recipient, String message) {
            System.out.printf("  [PUSH/%s → %s] %s%n", appId, recipient, message);
        }
        public String name() { return "Push/" + appId; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ABSTRACTION — hierarquia de notificações (varia independentemente)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Abstração base — contém a referência à implementação (o "bridge").
     */
    abstract static class Notification {
        protected final Channel channel;  // ← bridge

        Notification(Channel channel) {
            this.channel = channel;
        }

        abstract void notify(String recipient, String message);
    }

    /**
     * Abstração refinada — adiciona comportamento de urgência sobre qualquer canal.
     */
    static class UrgentNotification extends Notification {
        UrgentNotification(Channel channel) { super(channel); }

        @Override
        public void notify(String recipient, String message) {
            channel.send(recipient, "🚨 URGENTE: " + message.toUpperCase());
        }
    }

    /**
     * Abstração refinada — notificação regular com canal configurável.
     */
    static class RegularNotification extends Notification {
        RegularNotification(Channel channel) { super(channel); }

        @Override
        public void notify(String recipient, String message) {
            channel.send(recipient, message);
        }
    }

    /**
     * Abstração refinada — adiciona batching (envia via qualquer canal).
     * Demonstra que a abstração pode evoluir sem tocar nos canais.
     */
    static class BatchNotification extends Notification {
        BatchNotification(Channel channel) { super(channel); }

        void notifyAll(String[] recipients, String message) {
            System.out.printf("  [%s — Batch %d destinatários]%n",
                    channel.name(), recipients.length);
            for (var r : recipients) {
                channel.send(r, message);
            }
        }

        @Override
        public void notify(String recipient, String message) {
            channel.send(recipient, message);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Bridge Pattern ===\n");

        // Combinar qualquer abstração com qualquer canal sem novas subclasses
        var channels = new Channel[]{ new EmailChannel(), new SmsChannel(), new PushChannel("MyApp") };
        var message  = "Seu pedido foi confirmado";

        System.out.println("--- RegularNotification em todos os canais ---");
        for (var ch : channels) {
            new RegularNotification(ch).notify("ana@email.com", message);
        }

        System.out.println("\n--- UrgentNotification em todos os canais ---");
        for (var ch : channels) {
            new UrgentNotification(ch).notify("bob@email.com", "Pagamento recusado");
        }

        System.out.println("\n--- BatchNotification via Email ---");
        var batch = new BatchNotification(new EmailChannel());
        batch.notifyAll(
            new String[]{ "ana@email.com", "bob@email.com", "carlos@email.com" },
            "Promoção de fim de ano!"
        );

        System.out.println("\n✓ Adicionando Push/Android sem criar 3 novas subclasses de Notification:");
        var androidPush = new PushChannel("Android");
        new UrgentNotification(androidPush).notify("device-xyz", "Atenção: nova versão disponível");
    }
}
