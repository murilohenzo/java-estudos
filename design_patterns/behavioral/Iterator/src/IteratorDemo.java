import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * <h1>Iterator — Padrão Comportamental</h1>
 *
 * <h2>Intenção</h2>
 * <p>Fornecer uma forma de acessar sequencialmente os elementos de um objeto
 * agregado sem expor sua representação interna.
 *
 * <h2>Por que existe em Java</h2>
 * <p>Java tem {@code java.util.Iterator<E>} e o enhanced-for loop desde Java 5.
 * Mas implementar Iterator customizado ainda é relevante quando:
 * <ul>
 *   <li>Estrutura não é uma Collection padrão (árvore, grafo, paginação HTTP)</li>
 *   <li>Iteração lazy sobre fonte externa (banco, arquivo, stream de rede)</li>
 *   <li>Múltiplos cursores independentes sobre a mesma estrutura</li>
 *   <li>Iteração com filtro embutido sem copiar elementos</li>
 * </ul>
 *
 * <h2>Quando usar</h2>
 * <ul>
 *   <li>Coleções com múltiplas formas de travessia (DFS vs BFS em árvores)</li>
 *   <li>Abstração sobre fonte de dados: banco, arquivo, API paginada</li>
 *   <li>Iteração infinita (sequências matemáticas, IDs sequenciais)</li>
 * </ul>
 *
 * <h2>Java 21</h2>
 * <p>Iteradores customizados podem ser combinados com Streams via
 * {@code StreamSupport.stream(spliterator, false)} para trabalhar com toda
 * a API funcional existente.
 */
public class IteratorDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // Exemplo 1 — Árvore Binária com DFS e BFS iterators
    // ─────────────────────────────────────────────────────────────────────────

    static class BinaryTree<T> implements Iterable<T> {

        record Node<T>(T value, Node<T> left, Node<T> right) {
            // Folha
            Node(T value) { this(value, null, null); }
        }

        private Node<T> root;

        void setRoot(Node<T> root) { this.root = root; }

        /** In-order iterator padrão (DFS) — para enhanced-for */
        @Override
        public Iterator<T> iterator() { return inOrderIterator(); }

        /** In-order DFS: esquerda → raiz → direita */
        Iterator<T> inOrderIterator() {
            var stack = new java.util.ArrayDeque<Node<T>>();
            pushLeft(stack, root);

            return new Iterator<>() {
                @Override
                public boolean hasNext() { return !stack.isEmpty(); }

                @Override
                public T next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    var node = stack.pop();
                    pushLeft(stack, node.right());
                    return node.value();
                }
            };
        }

        /** BFS (Level-order) — necessita de fila */
        Iterator<T> breadthFirstIterator() {
            var queue = new java.util.ArrayDeque<Node<T>>();
            if (root != null) queue.offer(root);

            return new Iterator<>() {
                @Override
                public boolean hasNext() { return !queue.isEmpty(); }

                @Override
                public T next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    var node = queue.poll();
                    if (node.left()  != null) queue.offer(node.left());
                    if (node.right() != null) queue.offer(node.right());
                    return node.value();
                }
            };
        }

        private void pushLeft(java.util.Deque<Node<T>> stack, Node<T> node) {
            while (node != null) { stack.push(node); node = node.left(); }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Exemplo 2 — Iterator para paginação de API
    // ─────────────────────────────────────────────────────────────────────────

    record Page<T>(java.util.List<T> items, boolean hasMore, int pageNumber) {}

    @FunctionalInterface
    interface PageLoader<T> {
        Page<T> load(int pageNumber, int pageSize);
    }

    /** Iterator lazy que carrega páginas conforme necessário */
    static class PaginatedIterator<T> implements Iterator<T> {
        private final PageLoader<T> loader;
        private final int pageSize;
        private java.util.List<T> currentItems = java.util.List.of();
        private int itemIndex = 0;
        private int pageNumber = 0;
        private boolean hasMore = true;

        PaginatedIterator(PageLoader<T> loader, int pageSize) {
            this.loader   = loader;
            this.pageSize = pageSize;
            loadNext();
        }

        private void loadNext() {
            if (!hasMore) return;
            var page = loader.load(pageNumber++, pageSize);
            currentItems = page.items();
            hasMore = page.hasMore();
            itemIndex = 0;
            System.out.printf("    [Paginator] carregada página %d (%d itens)%n",
                    page.pageNumber(), page.items().size());
        }

        @Override
        public boolean hasNext() {
            return itemIndex < currentItems.size() || hasMore;
        }

        @Override
        public T next() {
            if (itemIndex >= currentItems.size()) {
                loadNext();
            }
            if (itemIndex >= currentItems.size()) throw new NoSuchElementException();
            return currentItems.get(itemIndex++);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Exemplo 3 — Filtered Iterator (sem criar coleção intermediária)
    // ─────────────────────────────────────────────────────────────────────────

    static class FilteredIterator<T> implements Iterator<T> {
        private final Iterator<T> source;
        private final Predicate<T> predicate;
        private T next;
        private boolean nextReady = false;

        FilteredIterator(Iterator<T> source, Predicate<T> predicate) {
            this.source    = source;
            this.predicate = predicate;
        }

        @Override
        public boolean hasNext() {
            while (!nextReady && source.hasNext()) {
                var candidate = source.next();
                if (predicate.test(candidate)) {
                    next = candidate;
                    nextReady = true;
                }
            }
            return nextReady;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            nextReady = false;
            return next;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Iterator Pattern ===\n");

        // ── Árvore binária ──
        System.out.println("--- 1. Binary Tree Iterators ---");
        //          4
        //        /   \
        //       2     6
        //      / \   / \
        //     1   3 5   7
        var tree = new BinaryTree<Integer>();
        tree.setRoot(new BinaryTree.Node<>(4,
                new BinaryTree.Node<>(2, new BinaryTree.Node<>(1), new BinaryTree.Node<>(3)),
                new BinaryTree.Node<>(6, new BinaryTree.Node<>(5), new BinaryTree.Node<>(7))));

        System.out.print("  In-order  (DFS): ");
        var inOrder = java.util.stream.StreamSupport.stream(
                java.util.Spliterators.spliteratorUnknownSize(tree.inOrderIterator(), 0), false);
        System.out.println(inOrder.toList());

        System.out.print("  Breadth-first:   ");
        var bfs = java.util.stream.StreamSupport.stream(
                java.util.Spliterators.spliteratorUnknownSize(tree.breadthFirstIterator(), 0), false);
        System.out.println(bfs.toList());

        System.out.print("  Enhanced-for:    ");
        for (var v : tree) System.out.print(v + " ");
        System.out.println();

        // ── Paginação ──
        System.out.println("\n--- 2. Paginated Iterator ---");
        var allData = java.util.List.of("A","B","C","D","E","F","G","H");
        PageLoader<String> loader = (page, size) -> {
            int from = page * size;
            int to   = Math.min(from + size, allData.size());
            return new Page<>(allData.subList(from, to), to < allData.size(), page);
        };

        var it = new PaginatedIterator<>(loader, 3);
        System.out.print("  Todos os itens: ");
        while (it.hasNext()) System.out.print(it.next() + " ");
        System.out.println();

        // ── Filtered ──
        System.out.println("\n--- 3. Filtered Iterator ---");
        var numbers = java.util.List.of(1,2,3,4,5,6,7,8,9,10);
        var evens   = new FilteredIterator<>(numbers.iterator(), n -> n % 2 == 0);
        System.out.print("  Pares: ");
        while (evens.hasNext()) System.out.print(evens.next() + " ");
        System.out.println();
    }
}
