package lambdas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.*;

/**
 * Catálogo das interfaces funcionais centrais de {@code java.util.function}.
 * Cada bloco mostra assinatura mental, composição e um caso de uso curto.
 */
public class FunctionalInterfacesCatalog {

    public static void main(String[] args) {
        System.out.println("=== Interfaces funcionais — núcleo ===\n");

        demoSupplier();
        demoConsumer();
        demoFunction();
        demoPredicate();
        demoUnaryAndBinary();
        demoBiVariants();
        demoCustomTriFunction();
    }

    static void demoSupplier() {
        System.out.println("--- Supplier<T>  () → T ---");
        Supplier<LocalDateTime> now = LocalDateTime::now;
        Supplier<List<String>> newList = ArrayList::new;

        System.out.println("now = " + now.get());
        List<String> list = newList.get();
        list.add("lazy factory");
        System.out.println("list = " + list);

        String value = Optional.<String>empty()
                .orElseGet(() -> "guest"); // Supplier só roda se vazio
        System.out.println("orElseGet = " + value);
        System.out.println();
    }

    static void demoConsumer() {
        System.out.println("--- Consumer<T>  T → void ---");
        StringBuilder audit = new StringBuilder();

        Consumer<String> print = System.out::println;
        Consumer<String> append = s -> audit.append(s).append(';');

        print.andThen(append).accept("pedido-42");
        System.out.println("audit = " + audit);
        System.out.println();
    }

    static void demoFunction() {
        System.out.println("--- Function<T,R>  T → R ---");
        Function<String, String> upper = String::toUpperCase;
        Function<String, Integer> len = String::length;
        Function<String, String> tag = s -> "[" + s + "]";

        Function<String, String> pipeline = upper.andThen(tag);
        System.out.println("andThen = " + pipeline.apply("java"));

        Function<String, Integer> lenOfUpper = upper.andThen(len);
        System.out.println("len(upper) = " + lenOfUpper.apply("java"));

        Function<String, Integer> composed = len.compose(upper);
        System.out.println("compose = " + composed.apply("java"));
        System.out.println();
    }

    static void demoPredicate() {
        System.out.println("--- Predicate<T>  T → boolean ---");
        Predicate<String> blank = String::isBlank;
        Predicate<String> longText = s -> s.length() > 8;
        Predicate<String> usable = blank.negate().and(longText);

        List<String> samples = List.of("", "oi", "texto-longo-ok", "short");
        System.out.println("usable = " + samples.stream().filter(usable).toList());
        System.out.println("isEqual = " + Predicate.isEqual("java").test("java"));
        System.out.println();
    }

    static void demoUnaryAndBinary() {
        System.out.println("--- UnaryOperator / BinaryOperator ---");
        UnaryOperator<String> trimUpper = s -> s.strip().toUpperCase();
        List<String> names = new ArrayList<>(List.of(" ana ", " bob "));
        names.replaceAll(trimUpper);
        System.out.println("replaceAll = " + names);

        BinaryOperator<Integer> sum = Integer::sum;
        BinaryOperator<Integer> max = BinaryOperator.maxBy(Comparator.naturalOrder());
        System.out.println("reduce sum = " + List.of(1, 2, 3).stream().reduce(0, sum));
        System.out.println("reduce max = " + List.of(1, 9, 3).stream().reduce(max).orElseThrow());
        System.out.println();
    }

    static void demoBiVariants() {
        System.out.println("--- BiFunction / BiConsumer / BiPredicate ---");

        BiFunction<String, Integer, String> repeat = String::repeat;
        System.out.println("repeat = " + repeat.apply("na", 3));

        BiFunction<BigDecimal, BigDecimal, BigDecimal> total =
                (price, qty) -> price.multiply(qty);
        System.out.println("total = " + total.apply(new BigDecimal("19.90"), new BigDecimal("2")));

        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Ana", 10);
        scores.put("Bob", 8);
        BiConsumer<String, Integer> printScore =
                (name, score) -> System.out.println("  " + name + " → " + score);
        scores.forEach(printScore);

        BiPredicate<String, String> sameIgnoreCase = String::equalsIgnoreCase;
        System.out.println("sameIgnoreCase = " + sameIgnoreCase.test("Java", "JAVA"));
        System.out.println();
    }

    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    static void demoCustomTriFunction() {
        System.out.println("--- TriFunction customizada ---");
        TriFunction<String, Integer, Boolean, String> format =
                (text, times, upper) -> {
                    String result = text.repeat(times);
                    return upper ? result.toUpperCase() : result;
                };
        System.out.println(format.apply("java ", 2, true));
        System.out.println();
    }
}
