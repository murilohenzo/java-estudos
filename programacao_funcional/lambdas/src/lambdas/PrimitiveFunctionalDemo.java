package lambdas;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;
import java.util.stream.IntStream;

/**
 * Especializações primitivas de {@code java.util.function}.
 * Preferir em hot paths e com {@link java.util.stream.IntStream} para evitar boxing.
 */
public class PrimitiveFunctionalDemo {

    public static void main(String[] args) {
        System.out.println("=== Interfaces funcionais — primitivas ===\n");

        demoSuppliersAndConsumers();
        demoPredicatesAndOperators();
        demoToAndFromObject();
        demoObjIntConsumer();
        demoBooleanSupplier();
    }

    static void demoSuppliersAndConsumers() {
        System.out.println("--- IntSupplier / IntConsumer ---");
        IntSupplier dice = () -> ThreadLocalRandom.current().nextInt(1, 7);
        IntConsumer print = n -> System.out.print(n + " ");

        System.out.print("rolls: ");
        IntStream.generate(dice).limit(5).forEach(print);
        System.out.println("\n");
    }

    static void demoPredicatesAndOperators() {
        System.out.println("--- IntPredicate / IntUnaryOperator / IntBinaryOperator ---");
        IntPredicate even = n -> n % 2 == 0;
        IntUnaryOperator square = n -> n * n;
        IntBinaryOperator add = Integer::sum;

        int[] data = {1, 2, 3, 4, 5, 6};
        int sumOfEvenSquares = Arrays.stream(data)
                .filter(even)
                .map(square)
                .reduce(0, add);

        System.out.println("sum of even squares = " + sumOfEvenSquares); // 2²+4²+6² = 56
        System.out.println();
    }

    static void demoToAndFromObject() {
        System.out.println("--- ToIntFunction / IntFunction ---");
        ToIntFunction<String> length = String::length;
        IntFunction<String> stars = n -> "*".repeat(n);

        List<String> words = List.of("a", "bb", "ccc");
        int totalChars = words.stream().mapToInt(length).sum();
        System.out.println("totalChars = " + totalChars);
        System.out.println("stars(5) = " + stars.apply(5));

        ToLongFunction<String> asLongHash = s -> Integer.toUnsignedLong(s.hashCode());
        DoubleFunction<String> format = d -> "value=%.2f".formatted(d);
        System.out.println("hashAsLong = " + asLongHash.applyAsLong("java"));
        System.out.println(format.apply(3.14159));
        System.out.println();
    }

    static void demoObjIntConsumer() {
        System.out.println("--- ObjIntConsumer<T>  (T, int) → void ---");
        List<String> letters = List.of("A", "B", "C");
        ObjIntConsumer<List<String>> indexedPrint =
                (list, index) -> System.out.println("  [" + index + "]=" + list.get(index));

        for (int i = 0; i < letters.size(); i++) {
            indexedPrint.accept(letters, i);
        }
        System.out.println();
    }

    static void demoBooleanSupplier() {
        System.out.println("--- BooleanSupplier ---");
        boolean featureEnabled = true;
        BooleanSupplier betaFlag = () -> featureEnabled;

        if (betaFlag.getAsBoolean()) {
            System.out.println("beta features ON");
        }
        System.out.println();
    }
}
