import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Mediator — Padrão Comportamental</h1>
 *
 * <h2>Intenção</h2>
 * <p>Definir um objeto que encapsula a forma como um conjunto de objetos interage.
 * O Mediator promove o fraco acoplamento ao evitar que os objetos se referenciem
 * explicitamente, permitindo variar suas interações independentemente.
 *
 * <h2>Problema — acoplamento N×N</h2>
 * <p>Sem Mediator, N objetos que se comunicam geram N×(N-1) conexões.
 * Com Mediator, cada objeto conhece apenas o Mediator: N conexões totais.
 * <pre>
 *   Sem: A↔B, A↔C, B↔C, A↔D, B↔D, C↔D = 6 conexões para 4 objetos
 *   Com: A→M, B→M, C→M, D→M = 4 conexões
 * </pre>
 *
 * <h2>Quando usar</h2>
 * <ul>
 *   <li>Componentes UI que se afetam mutuamente (ativar botão quando checkbox está marcado)</li>
 *   <li>Chat rooms, sistemas de mensageria pub/sub</li>
 *   <li>Controle de tráfego aéreo — torre controla todos os aviões</li>
 *   <li>Quando há muitas dependências entre objetos tornando-os difíceis de reusar</li>
 * </ul>
 *
 * <h2>Diferença Mediator vs Observer</h2>
 * <ul>
 *   <li><b>Mediator</b>: coordena interações bilaterais entre componentes específicos</li>
 *   <li><b>Observer</b>: notifica assinantes sobre eventos; a comunicação é unidirecional</li>
 * </ul>
 */
public class MediatorDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // MEDIATOR — interface
    // ─────────────────────────────────────────────────────────────────────────

    interface ChatMediator {
        void register(ChatUser user);
        void send(String message, ChatUser sender);
        void sendTo(String message, ChatUser sender, String targetName);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COLLEAGUE — usuário de chat
    // ─────────────────────────────────────────────────────────────────────────

    static class ChatUser {
        private final String name;
        private final ChatMediator mediator;
        private final List<String> inbox = new ArrayList<>();

        ChatUser(String name, ChatMediator mediator) {
            this.name     = name;
            this.mediator = mediator;
            mediator.register(this);
        }

        /** Envia mensagem para todos via mediator — não conhece outros usuários */
        void broadcast(String message) {
            System.out.printf("  [%s → TODOS] %s%n", name, message);
            mediator.send(message, this);
        }

        /** Envia mensagem privada via mediator */
        void whisper(String targetName, String message) {
            System.out.printf("  [%s → %s] %s%n", name, targetName, message);
            mediator.sendTo(message, this, targetName);
        }

        /** Recebe mensagem de outro usuário (chamado pelo mediator) */
        void receive(String message, String senderName) {
            var entry = "[%s → %s]: %s".formatted(senderName, name, message);
            inbox.add(entry);
        }

        String name()        { return name; }
        List<String> inbox() { return List.copyOf(inbox); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONCRETE MEDIATOR — sala de chat
    // ─────────────────────────────────────────────────────────────────────────

    static class ChatRoom implements ChatMediator {
        private final String roomName;
        private final List<ChatUser> users = new ArrayList<>();
        private final List<String> history = new ArrayList<>();

        ChatRoom(String roomName) { this.roomName = roomName; }

        @Override
        public void register(ChatUser user) {
            users.add(user);
            System.out.printf("  [%s] '%s' entrou na sala%n", roomName, user.name());
        }

        @Override
        public void send(String message, ChatUser sender) {
            var log = "[%s] %s: %s".formatted(roomName, sender.name(), message);
            history.add(log);
            // Entrega para todos exceto o remetente
            users.stream()
                    .filter(u -> !u.equals(sender))
                    .forEach(u -> u.receive(message, sender.name()));
        }

        @Override
        public void sendTo(String message, ChatUser sender, String targetName) {
            users.stream()
                    .filter(u -> u.name().equals(targetName))
                    .findFirst()
                    .ifPresentOrElse(
                            target -> {
                                target.receive(message, sender.name());
                                history.add("[PM] %s→%s: %s".formatted(sender.name(), targetName, message));
                            },
                            () -> System.out.printf("  [%s] usuário '%s' não encontrado%n", roomName, targetName)
                    );
        }

        void printHistory() {
            System.out.println("\n  --- Histórico da sala '%s' ---".formatted(roomName));
            history.forEach(e -> System.out.println("    " + e));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Mediator Pattern — Chat Room ===\n");

        var room = new ChatRoom("java-estudos");

        var ana    = new ChatUser("Ana",    room);
        var bob    = new ChatUser("Bob",    room);
        var carlos = new ChatUser("Carlos", room);

        System.out.println("\n--- Mensagens ---");
        ana.broadcast("Olá pessoal!");
        bob.broadcast("Oi Ana! Tudo bem?");
        carlos.whisper("Ana", "Preciso de ajuda com o Mediator");
        ana.whisper("Carlos", "Claro! O segredo é: nenhum objeto fala diretamente com o outro");
        bob.broadcast("Alguém quer fazer pair programming?");

        room.printHistory();

        System.out.println("\n--- Inbox de cada usuário ---");
        for (var user : List.of(ana, bob, carlos)) {
            System.out.println("  " + user.name() + ": " + user.inbox());
        }
    }
}
