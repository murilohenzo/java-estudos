import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * <h1>Memento — Padrão Comportamental</h1>
 *
 * <h2>Intenção</h2>
 * <p>Capturar e externalizar o estado interno de um objeto sem violar o encapsulamento,
 * de forma que o objeto possa ser restaurado a esse estado posteriormente.
 *
 * <h2>Componentes</h2>
 * <ul>
 *   <li><b>Originator</b> — objeto cujo estado é salvo; cria e usa Mementos</li>
 *   <li><b>Memento</b> — snapshot imutável do estado do Originator; opaco para o Caretaker</li>
 *   <li><b>Caretaker</b> — guarda Mementos; não inspeciona nem modifica o conteúdo</li>
 * </ul>
 *
 * <h2>Invariante fundamental</h2>
 * <p>O Caretaker não deve nunca acessar o conteúdo interno do Memento —
 * apenas o Originator conhece e interpreta seu estado.
 *
 * <h2>Quando usar</h2>
 * <ul>
 *   <li>Undo/Redo que precisa restaurar estado complexo (diferente do Command)</li>
 *   <li>Checkpoints em jogos, transações com rollback, snapshots de configuração</li>
 *   <li>Quando expor o estado do Originator para salvar violaria encapsulamento</li>
 * </ul>
 *
 * <h2>Diferença Memento vs Command</h2>
 * <ul>
 *   <li><b>Command</b>: armazena operações reversíveis (como desfazer uma inserção)</li>
 *   <li><b>Memento</b>: armazena snapshots completos do estado (mais simples para objetos complexos)</li>
 * </ul>
 *
 * <h2>Java 21</h2>
 * <p>O Memento é implementado como um {@code record} — imutável por construção,
 * garantindo que o estado capturado não muda depois de salvo.
 */
public class MementoDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // MEMENTO — snapshot imutável (record = imutável por padrão em Java 21)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * O record Memento expõe apenas metadados (timestamp, label) ao Caretaker.
     * O conteúdo real (state) é opaco para quem não é o Originator.
     */
    record FormSnapshot(
            GameState state,   // dados internos — apenas Originator os usa
            Instant savedAt,
            String label
    ) {
        @Override
        public String toString() {
            return "[%s] %s".formatted(savedAt.toString().substring(11, 19), label);
        }
    }

    // Estado interno do Originator — agrupado em record para clareza
    record GameState(
            String playerName,
            int    level,
            int    health,
            int    score,
            String location
    ) {}

    // ─────────────────────────────────────────────────────────────────────────
    // ORIGINATOR — o "jogo" cujo estado é salvo e restaurado
    // ─────────────────────────────────────────────────────────────────────────

    static class Game {
        private String playerName;
        private int    level;
        private int    health;
        private int    score;
        private String location;

        Game(String playerName) {
            this.playerName = playerName;
            this.level      = 1;
            this.health     = 100;
            this.score      = 0;
            this.location   = "Início";
        }

        // ── Ações do jogo ─────────────────────────────────────────────────────

        void advanceLevel(String newLocation) {
            level++;
            score += 1000;
            health = Math.min(100, health + 30);
            location = newLocation;
            System.out.println("  ↑ Level up! " + this);
        }

        void takeDamage(int dmg) {
            health = Math.max(0, health - dmg);
            System.out.println("  💥 Dano " + dmg + " | " + this);
        }

        void collectTreasure(int points) {
            score += points;
            System.out.println("  💰 +" + points + " pontos | " + this);
        }

        // ── Memento ──────────────────────────────────────────────────────────

        /** Cria um snapshot imutável do estado atual. */
        FormSnapshot save(String label) {
            var snap = new FormSnapshot(
                    new GameState(playerName, level, health, score, location),
                    Instant.now(),
                    label
            );
            System.out.println("  💾 Salvo: " + snap);
            return snap;
        }

        /** Restaura o estado a partir de um Memento. */
        void restore(FormSnapshot snapshot) {
            var s    = snapshot.state();
            playerName = s.playerName();
            level      = s.level();
            health     = s.health();
            score      = s.score();
            location   = s.location();
            System.out.println("  ↩ Restaurado: " + snapshot + " | " + this);
        }

        @Override
        public String toString() {
            return "Game{player='%s', level=%d, hp=%d, score=%d, loc='%s'}"
                    .formatted(playerName, level, health, score, location);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CARETAKER — gerencia checkpoints sem conhecer conteúdo
    // ─────────────────────────────────────────────────────────────────────────

    static class SaveManager {
        private final Deque<FormSnapshot> saves = new ArrayDeque<>();

        void push(FormSnapshot snapshot) { saves.push(snapshot); }

        FormSnapshot pop() {
            if (saves.isEmpty()) throw new IllegalStateException("Nenhum save disponível");
            return saves.pop();
        }

        FormSnapshot peek() {
            if (saves.isEmpty()) throw new IllegalStateException("Nenhum save disponível");
            return saves.peek();
        }

        List<FormSnapshot> history() { return saves.stream().toList(); }
        int size() { return saves.size(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Memento Pattern — Game Checkpoints ===\n");

        var game    = new Game("Ana");
        var manager = new SaveManager();

        System.out.println("Estado inicial: " + game);

        // Checkpoint 1
        System.out.println("\n--- Fase 1 ---");
        game.advanceLevel("Floresta");
        game.collectTreasure(500);
        manager.push(game.save("Checkpoint 1 — Floresta"));

        // Avançar
        System.out.println("\n--- Fase 2 ---");
        game.advanceLevel("Caverna");
        game.takeDamage(40);
        game.collectTreasure(1200);
        manager.push(game.save("Checkpoint 2 — Caverna"));

        // Boss fight — toma muito dano
        System.out.println("\n--- Boss Fight ---");
        game.takeDamage(70);
        game.takeDamage(30);
        System.out.println("  💀 HP zerado! Restaurando último checkpoint...");

        // Restaurar último save
        game.restore(manager.pop());

        // Continua desde o checkpoint
        System.out.println("\n--- Retomando da Caverna ---");
        game.collectTreasure(300);
        game.advanceLevel("Castelo");

        // Listar histórico
        System.out.println("\n--- Saves disponíveis ---");
        manager.history().forEach(s -> System.out.println("  " + s));
        System.out.println("Total de saves: " + manager.size());

        System.out.println("\nEstado final: " + game);
    }
}
