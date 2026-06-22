package br.com.murilo.collections;

import java.util.*;

/**
 * Demonstração de <b>Sequenced Collections</b> — JEP 431 (GA no Java 21).
 *
 * <h2>Problema que resolve</h2>
 * <p>Antes do Java 21, não havia uma interface unificada para coleções com
 * <em>ordem definida</em> que fornecesse acesso ao primeiro e último elemento.
 * Cada implementação usava uma API diferente:
 *
 * <pre>
 *   List.get(0)              // primeiro de List
 *   List.get(list.size()-1)  // último de List
 *   Deque.getFirst()         // primeiro de Deque
 *   Deque.getLast()          // último de Deque
 *   SortedSet.first()        // primeiro de SortedSet
 *   SortedSet.last()         // último de SortedSet
 * </pre>
 *
 * <h2>Nova hierarquia de interfaces</h2>
 * <pre>
 *   SequencedCollection<E>
 *     ├── List<E>
 *     └── SequencedSet<E>
 *           ├── SortedSet<E>
 *           └── LinkedHashSet<E>  (via implementação)
 *
 *   SequencedMap<K,V>
 *     └── SortedMap<K,V>
 *           └── LinkedHashMap<K,V>  (via implementação)
 * </pre>
 *
 * <h2>API unificada</h2>
 * <ul>
 *   <li>{@code getFirst()} / {@code getLast()} — acesso ao primeiro/último</li>
 *   <li>{@code addFirst(e)} / {@code addLast(e)} — inserção no início/fim</li>
 *   <li>{@code removeFirst()} / {@code removeLast()} — remoção do início/fim</li>
 *   <li>{@code reversed()} — visão invertida sem criar nova coleção</li>
 * </ul>
 */
public class SequencedCollectionsDemo {

    public static void main(String[] args) {
        System.out.println("=== Sequenced Collections — Java 21 ===\n");

        demonstrateSequencedCollection();
        demonstrateSequencedSet();
        demonstrateSequencedMap();
        demonstrateReversed();
        demonstratePolymorphism();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. SequencedCollection — List
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateSequencedCollection() {
        System.out.println("--- 1. SequencedCollection com ArrayList ---");

        SequencedCollection<String> languages = new ArrayList<>(
                List.of("Python", "JavaScript", "Kotlin")
        );

        // Antes do Java 21: languages.get(0), languages.get(languages.size()-1)
        System.out.println("First: " + languages.getFirst());  // Python
        System.out.println("Last:  " + languages.getLast());   // Kotlin

        // Adiciona no início e no fim
        languages.addFirst("Java");   // ["Java", "Python", "JavaScript", "Kotlin"]
        languages.addLast("Go");      // ["Java", "Python", "JavaScript", "Kotlin", "Go"]
        System.out.println("After add: " + languages);

        // Remove do início e fim
        String removed = languages.removeFirst();             // remove "Java"
        System.out.println("Removed first: " + removed + " → " + languages);
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. SequencedSet — LinkedHashSet mantém ordem de inserção
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateSequencedSet() {
        System.out.println("--- 2. SequencedSet com LinkedHashSet ---");

        SequencedSet<String> tags = new LinkedHashSet<>(
                List.of("java", "backend", "oop", "solid")
        );

        System.out.println("First: " + tags.getFirst());  // "java"
        System.out.println("Last:  " + tags.getLast());   // "solid"

        tags.addFirst("programming");   // inserção no início
        System.out.println("After addFirst: " + tags);

        // reversed() — visão invertida, sem criar nova coleção (lazy)
        System.out.println("Reversed: " + tags.reversed());
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. SequencedMap — LinkedHashMap mantém ordem de inserção
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateSequencedMap() {
        System.out.println("--- 3. SequencedMap com LinkedHashMap ---");

        SequencedMap<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Ana",    95);
        scores.put("Bob",    87);
        scores.put("Carlos", 92);

        // Antes do Java 21: precisava de iterator ou stream
        Map.Entry<String, Integer> first = scores.firstEntry();
        Map.Entry<String, Integer> last  = scores.lastEntry();

        System.out.println("First entry: " + first);  // Ana=95
        System.out.println("Last entry:  " + last);   // Carlos=92

        // Inserção posicional
        scores.putFirst("Zara", 99);   // insere no início
        scores.putLast("Zero", 10);    // insere no fim
        System.out.println("After put: " + scores);

        // Remoção posicional
        var removed = scores.pollFirstEntry();
        System.out.println("Removed first: " + removed);

        // reversed() na map — visão reversa das entradas
        System.out.println("Reversed:  " + scores.reversed());
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. reversed() — Visão invertida lazy
    // ─────────────────────────────────────────────────────────────────────────

    static void demonstrateReversed() {
        System.out.println("--- 4. reversed() — visão invertida sem cópia ---");

        var list = new ArrayList<>(List.of(1, 2, 3, 4, 5));

        // reversed() retorna uma VIEW — não cria nova lista
        SequencedCollection<Integer> rev = list.reversed();
        System.out.println("Original: " + list);     // [1, 2, 3, 4, 5]
        System.out.println("Reversed: " + rev);       // [5, 4, 3, 2, 1]

        // Modificar a view modifica o original (e vice-versa)
        rev.addFirst(0);
        System.out.println("Add 0 to reversed first: " + rev);   // [0, 5, 4, 3, 2, 1]
        System.out.println("Original after:          " + list);  // [1, 2, 3, 4, 5, 0]

        // Iterar em ordem reversa sem Collections.reverse()
        System.out.print("Reversed forEach: ");
        list.reversed().forEach(n -> System.out.print(n + " "));
        System.out.println("\n");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Polimorfismo — método aceita qualquer SequencedCollection
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Antes do Java 21, para aceitar List e Deque com getFirst/getLast,
     * eram necessários overloads separados. Agora, uma única assinatura serve.
     */
    static <T> void printBoundaries(SequencedCollection<T> collection) {
        if (collection.isEmpty()) { System.out.println("empty"); return; }
        System.out.printf("  First=%-10s Last=%s%n",
                collection.getFirst(), collection.getLast());
    }

    static void demonstratePolymorphism() {
        System.out.println("--- 5. Polimorfismo com SequencedCollection ---");

        printBoundaries(new ArrayList<>(List.of("A", "B", "C")));         // List
        printBoundaries(new LinkedList<>(List.of(10, 20, 30)));            // Deque/List
        printBoundaries(new LinkedHashSet<>(Set.of("x", "y", "z")));      // Set
        printBoundaries(new ArrayDeque<>(List.of(1.0, 2.0, 3.0)));        // Deque
    }
}
