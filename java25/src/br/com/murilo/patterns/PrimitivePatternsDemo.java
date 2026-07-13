package br.com.murilo.patterns;

/**
 * Demonstração de <b>Primitive Types in Patterns</b> — JEP 507 (3ª Preview no Java 25).
 *
 * <p>Patterns / {@code switch} / {@code instanceof} com tipos primitivos,
 * conforme a motivação oficial da JEP.
 *
 * <pre>
 * javac --enable-preview --release 25 ...
 * java  --enable-preview -cp . br.com.murilo.patterns.PrimitivePatternsDemo
 * </pre>
 */
public class PrimitivePatternsDemo {

    record Measurement(double value) {}

    public static void main(String[] args) {
        System.out.println("=== Primitive Patterns — Java 25 / JEP 507 (Preview) ===\n");

        for (int status : new int[]{0, 1, 2, 99}) {
            System.out.println("status " + status + " → " + describeStatus(status));
        }

        System.out.println();
        for (int flights : new int[]{50, 150, 400}) {
            System.out.println("flights " + flights + " → " + airlineTier(flights));
        }

        System.out.println();
        Object boxed = 42;
        if (boxed instanceof int i) {
            System.out.println("instanceof int i = " + i);
        }

        Object m = new Measurement(3.14);
        if (m instanceof Measurement(double v)) {
            System.out.println("record primitive component = " + v);
        }

        System.out.println("\nCaso de uso: switch/patterns uniformes para primitivos e referências.");
    }

    /** Exemplo da JEP: default vira case com pattern primitivo. */
    static String describeStatus(int status) {
        return switch (status) {
            case 0 -> "okay";
            case 1 -> "warning";
            case 2 -> "error";
            case int i -> "unknown status: " + i;
        };
    }

    /** Guards sobre pattern primitivo. */
    static String airlineTier(int yearlyFlights) {
        return switch (yearlyFlights) {
            case int f when f >= 300 -> "platinum";
            case int f when f >= 100 -> "gold";
            case int f -> "standard (" + f + ")";
        };
    }
}
