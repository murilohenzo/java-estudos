import java.util.ArrayDeque;
import java.util.Deque;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Command — Padrão Comportamental</h1>
 *
 * <h2>Intenção</h2>
 * <p>Encapsular uma solicitação como um objeto, permitindo parametrizar clientes
 * com diferentes solicitações, enfileirar ou registrar solicitações e suportar
 * operações que podem ser desfeitas (undo/redo).
 *
 * <h2>Componentes</h2>
 * <ul>
 *   <li><b>Command</b> — interface com {@code execute()} e {@code undo()}</li>
 *   <li><b>ConcreteCommand</b> — implementa a ação; guarda referência ao Receiver e estado para undo</li>
 *   <li><b>Receiver</b> — objeto que realiza o trabalho real</li>
 *   <li><b>Invoker</b> — dispara o comando; mantém histórico para undo/redo</li>
 *   <li><b>Client</b> — cria e configura os commands</li>
 * </ul>
 *
 * <h2>Quando usar</h2>
 * <ul>
 *   <li>Undo/Redo (editores de texto, IDEs, Photoshop)</li>
 *   <li>Fila de tarefas, job queues, transações reversíveis</li>
 *   <li>Macros / gravação de operações para replay</li>
 *   <li>Logging de auditoria de ações do usuário</li>
 * </ul>
 *
 * <h2>Java 21</h2>
 * <p>Sealed interface para o Command garante que todos os tipos são conhecidos
 * e permite usar switch exaustivo no invoker para estatísticas de comandos.
 */
public class CommandDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // RECEIVER — editor de texto
    // ─────────────────────────────────────────────────────────────────────────

    static class TextEditor {
        private final StringBuilder text = new StringBuilder();

        void insertAt(int pos, String str) {
            text.insert(Math.min(pos, text.length()), str);
        }

        void deleteAt(int pos, int length) {
            int end = Math.min(pos + length, text.length());
            text.delete(pos, end);
        }

        String getText() { return text.toString(); }

        @Override public String toString() { return "\"" + text + "\""; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COMMAND — interface sealed (Java 21)
    // ─────────────────────────────────────────────────────────────────────────

    sealed interface EditorCommand permits InsertCommand, DeleteCommand, ReplaceCommand {
        void execute();
        void undo();
        String description();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONCRETE COMMANDS
    // ─────────────────────────────────────────────────────────────────────────

    static final class InsertCommand implements EditorCommand {
        private final TextEditor editor;
        private final int position;
        private final String text;

        InsertCommand(TextEditor editor, int position, String text) {
            this.editor = editor;
            this.position = position;
            this.text = text;
        }

        @Override public void execute() { editor.insertAt(position, text); }
        @Override public void undo()    { editor.deleteAt(position, text.length()); }
        @Override public String description() {
            return "Insert('%s' em pos %d)".formatted(text, position);
        }
    }

    static final class DeleteCommand implements EditorCommand {
        private final TextEditor editor;
        private final int position;
        private final int length;
        private String deleted;  // estado salvo para undo

        DeleteCommand(TextEditor editor, int position, int length) {
            this.editor   = editor;
            this.position = position;
            this.length   = length;
        }

        @Override
        public void execute() {
            // Salva o texto deletado ANTES de deletar (necessário para undo)
            var t = editor.getText();
            int end = Math.min(position + length, t.length());
            deleted = t.substring(position, end);
            editor.deleteAt(position, length);
        }

        @Override public void undo() { editor.insertAt(position, deleted); }
        @Override public String description() {
            return "Delete(pos %d, len %d)".formatted(position, length);
        }
    }

    static final class ReplaceCommand implements EditorCommand {
        private final TextEditor editor;
        private final int position;
        private final String newText;
        private String oldText;

        ReplaceCommand(TextEditor editor, int position, String newText) {
            this.editor   = editor;
            this.position = position;
            this.newText  = newText;
        }

        @Override
        public void execute() {
            var t = editor.getText();
            int end = Math.min(position + newText.length(), t.length());
            oldText = t.substring(position, end);
            editor.deleteAt(position, newText.length());
            editor.insertAt(position, newText);
        }

        @Override public void undo() {
            editor.deleteAt(position, newText.length());
            editor.insertAt(position, oldText);
        }

        @Override public String description() {
            return "Replace(pos %d, '%s')".formatted(position, newText);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INVOKER — gerencia histórico, undo e redo
    // ─────────────────────────────────────────────────────────────────────────

    static class EditorHistory {
        private final Deque<EditorCommand> undoStack = new ArrayDeque<>();
        private final Deque<EditorCommand> redoStack = new ArrayDeque<>();

        void execute(EditorCommand cmd) {
            cmd.execute();
            undoStack.push(cmd);
            redoStack.clear();  // nova ação invalida redo
            System.out.printf("  ✓ %s%n", cmd.description());
        }

        boolean undo() {
            if (undoStack.isEmpty()) { System.out.println("  [Undo] nada para desfazer"); return false; }
            var cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
            System.out.printf("  ↩ Undo: %s%n", cmd.description());
            return true;
        }

        boolean redo() {
            if (redoStack.isEmpty()) { System.out.println("  [Redo] nada para refazer"); return false; }
            var cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
            System.out.printf("  ↪ Redo: %s%n", cmd.description());
            return true;
        }

        // Java 21 — switch exaustivo sobre sealed hierarchy
        void printStats() {
            System.out.println("\n  --- Stats ---");
            long inserts = 0, deletes = 0, replaces = 0;
            for (var cmd : undoStack) {
                switch (cmd) {
                    case InsertCommand  c -> inserts++;
                    case DeleteCommand  c -> deletes++;
                    case ReplaceCommand c -> replaces++;
                }
            }
            System.out.printf("  Inserts: %d | Deletes: %d | Replaces: %d%n",
                    inserts, deletes, replaces);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Command Pattern — Text Editor ===\n");

        var editor  = new TextEditor();
        var history = new EditorHistory();

        System.out.println("--- Executando comandos ---");
        history.execute(new InsertCommand(editor, 0, "Hello"));
        System.out.println("  Editor: " + editor);

        history.execute(new InsertCommand(editor, 5, " World"));
        System.out.println("  Editor: " + editor);

        history.execute(new ReplaceCommand(editor, 6, "Java!"));
        System.out.println("  Editor: " + editor);

        history.execute(new DeleteCommand(editor, 0, 6));
        System.out.println("  Editor: " + editor);

        System.out.println("\n--- Undo 2× ---");
        history.undo(); System.out.println("  Editor: " + editor);
        history.undo(); System.out.println("  Editor: " + editor);

        System.out.println("\n--- Redo 1× ---");
        history.redo(); System.out.println("  Editor: " + editor);

        System.out.println("\n--- Nova ação invalida redo ---");
        history.execute(new InsertCommand(editor, 0, "[NOVO] "));
        System.out.println("  Editor: " + editor);
        history.redo();  // nada para refazer

        history.printStats();
    }
}
