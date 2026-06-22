import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Composite — Padrão Estrutural</h1>
 *
 * <h2>Intenção</h2>
 * <p>Compor objetos em estruturas de árvore para representar hierarquias parte-todo.
 * Permite que clientes tratem objetos individuais e composições de objetos de maneira uniforme.
 *
 * <h2>Componentes</h2>
 * <ul>
 *   <li><b>Component</b> — interface comum para folhas e composições</li>
 *   <li><b>Leaf</b> — objeto sem filhos; executa o trabalho real</li>
 *   <li><b>Composite</b> — contém filhos (Leaf ou outros Composite); delega para os filhos</li>
 *   <li><b>Client</b> — manipula elementos via interface Component</li>
 * </ul>
 *
 * <h2>Estrutura</h2>
 * <pre>
 *   Component
 *   ├── Leaf (arquivo, item de menu, pixel...)
 *   └── Composite (diretório, menu, frame...)
 *         ├── Leaf
 *         └── Composite
 *               ├── Leaf
 *               └── Leaf
 * </pre>
 *
 * <h2>Quando usar</h2>
 * <ul>
 *   <li>Sistemas de arquivos, menus hierárquicos, DOM, cenas gráficas</li>
 *   <li>Quando a lógica de negócio deve tratar itens simples e grupos identicamente</li>
 *   <li>Expressões aritméticas, árvores de UI</li>
 * </ul>
 *
 * <h2>Java 21</h2>
 * <p>Sealed interface garante exaustividade no switch — o compilador verifica que
 * todos os subtipos (File, Directory) são tratados.
 */
public class CompositeDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // COMPONENT — interface comum (sealed = hierarquia fechada e segura)
    // ─────────────────────────────────────────────────────────────────────────

    sealed interface FileSystemNode permits FileNode, DirectoryNode {
        String name();
        long sizeInBytes();
        void print(String indent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEAF — arquivo (sem filhos)
    // ─────────────────────────────────────────────────────────────────────────

    record FileNode(String name, long sizeInBytes) implements FileSystemNode {
        public void print(String indent) {
            System.out.printf("%s📄 %s (%s)%n", indent, name, formatSize(sizeInBytes));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COMPOSITE — diretório (contém filhos)
    // ─────────────────────────────────────────────────────────────────────────

    static class DirectoryNode implements FileSystemNode {
        private final String name;
        private final List<FileSystemNode> children = new ArrayList<>();

        DirectoryNode(String name) { this.name = name; }

        void add(FileSystemNode node) { children.add(node); }
        void remove(FileSystemNode node) { children.remove(node); }

        @Override
        public String name() { return name; }

        /**
         * O Composite delega para cada filho — recursivo por natureza.
         * Folha retorna seu tamanho; Composite soma todos os filhos.
         */
        @Override
        public long sizeInBytes() {
            return children.stream()
                    .mapToLong(FileSystemNode::sizeInBytes)
                    .sum();
        }

        @Override
        public void print(String indent) {
            System.out.printf("%s📁 %s/ (%s)%n", indent, name, formatSize(sizeInBytes()));
            for (var child : children) {
                child.print(indent + "  ");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Operações polimórficas usando Java 21 pattern matching
    // ─────────────────────────────────────────────────────────────────────────

    static long countFiles(FileSystemNode node) {
        return switch (node) {
            case FileNode f   -> 1;
            case DirectoryNode d -> d.children.stream()
                    .mapToLong(CompositeDemo::countFiles)
                    .sum();
        };
    }

    static List<FileNode> findByExtension(FileSystemNode node, String ext) {
        return switch (node) {
            case FileNode f      when f.name().endsWith(ext) -> List.of(f);
            case FileNode f      -> List.of();
            case DirectoryNode d -> d.children.stream()
                    .flatMap(child -> findByExtension(child, ext).stream())
                    .toList();
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Composite Pattern — File System ===\n");

        // Construir hierarquia
        var root = new DirectoryNode("projeto");

        var src = new DirectoryNode("src");
        var main = new DirectoryNode("main");
        main.add(new FileNode("Main.java", 2_048));
        main.add(new FileNode("Config.java", 1_024));
        var resources = new DirectoryNode("resources");
        resources.add(new FileNode("application.yml", 512));
        resources.add(new FileNode("logo.png", 45_000));
        src.add(main);
        src.add(resources);

        var test = new DirectoryNode("test");
        test.add(new FileNode("MainTest.java", 3_072));
        test.add(new FileNode("ConfigTest.java", 1_536));

        var docs = new DirectoryNode("docs");
        docs.add(new FileNode("README.md", 8_192));
        docs.add(new FileNode("TRADEOFFS.md", 4_096));

        root.add(src);
        root.add(test);
        root.add(docs);
        root.add(new FileNode("pom.xml", 2_560));

        // Imprimir árvore — cliente trata FileNode e DirectoryNode identicamente
        System.out.println("--- Estrutura ---");
        root.print("");

        System.out.println("\n--- Estatísticas ---");
        System.out.println("Total de arquivos:  " + countFiles(root));
        System.out.println("Tamanho total:      " + formatSize(root.sizeInBytes()));

        System.out.println("\n--- Buscar .java ---");
        findByExtension(root, ".java")
                .forEach(f -> System.out.printf("  %s (%s)%n", f.name(), formatSize(f.sizeInBytes())));
    }

    static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return "%.1f KB".formatted(bytes / 1024.0);
        return "%.1f MB".formatted(bytes / (1024.0 * 1024));
    }
}
