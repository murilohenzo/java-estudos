package br.com.murilo.patterns;

/**
 * Demonstração de <b>Pattern Matching for switch</b> — JEP 441 (GA no Java 21).
 *
 * <h2>O que é Pattern Matching for switch?</h2>
 * <p>Permite usar padrões de tipo, guarda-clauses ({@code when}) e padrões de registro
 * diretamente nas expressões {@code switch}, eliminando cascatas de {@code instanceof} + cast.
 *
 * <h2>Evolução histórica</h2>
 * <pre>
 *   Java 14: switch expressions (arrow syntax)  — JEP 361 GA
 *   Java 16: pattern matching instanceof        — JEP 394 GA
 *   Java 17: sealed classes                     — JEP 409 GA
 *   Java 21: pattern matching for switch        — JEP 441 GA  ← este demo
 *   Java 21: record patterns                    — JEP 440 GA  ← este demo
 * </pre>
 */
public class PatternMatchingSwitchDemo {

    // ─────────────────────────────────────────────────────────────────────────
    // Domínio de exemplo — hierarquia sealed
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sealed interface — define o conjunto fechado de subtipos.
     * O compilador exige que o switch cubra TODOS os casos permitidos.
     */
    sealed interface Shape permits Circle, Rectangle, Triangle {}

    record Circle(double radius)                   implements Shape {}
    record Rectangle(double width, double height)  implements Shape {}
    record Triangle(double base, double height)    implements Shape {}

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Switch com Type Patterns
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Pattern matching for switch — elimina cascata de instanceof + cast.
     *
     * <p><b>Antes (Java 16 pattern matching instanceof, mas sem switch pattern):</b>
     * <pre>{@code
     *   if (shape instanceof Circle c)         { return Math.PI * c.radius() * c.radius(); }
     *   else if (shape instanceof Rectangle r) { return r.width() * r.height(); }
     *   else if (shape instanceof Triangle t)  { return 0.5 * t.base() * t.height(); }
     *   else throw new IllegalArgumentException();
     * }</pre>
     *
     * <p><b>Agora (Java 21):</b>
     */
    static double area(Shape shape) {
        return switch (shape) {
            case Circle c     -> Math.PI * c.radius() * c.radius();
            case Rectangle r  -> r.width() * r.height();
            case Triangle t   -> 0.5 * t.base() * t.height();
            // Sem default necessário — sealed garante cobertura exaustiva
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Guarded Patterns — cláusula `when`
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * {@code when} adiciona uma condição de guarda ao padrão.
     * A guarda é avaliada SOMENTE se o padrão de tipo já combinou.
     */
    static String classify(Shape shape) {
        return switch (shape) {
            case Circle c when c.radius() > 100   -> "círculo grande (r > 100)";
            case Circle c when c.radius() > 10    -> "círculo médio (10 < r ≤ 100)";
            case Circle c                          -> "círculo pequeno (r ≤ 10)";
            case Rectangle r when r.width() == r.height() -> "quadrado";
            case Rectangle r                              -> "retângulo";
            case Triangle t when t.base() == t.height()   -> "triângulo isósceles (aprox.)";
            case Triangle t                               -> "triângulo genérico";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Pattern Matching com null — null agora pode ser case
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Antes do Java 21, switch lançava NullPointerException para null.
     * Agora, {@code case null} é tratado explicitamente.
     */
    static String describe(Object obj) {
        return switch (obj) {
            case null              -> "valor nulo";
            case Integer i when i < 0 -> "inteiro negativo: " + i;
            case Integer i         -> "inteiro positivo: " + i;
            case String s when s.isBlank() -> "string vazia";
            case String s          -> "string: '" + s + "'";
            case int[] arr         -> "array de int com %d elementos".formatted(arr.length);
            default                -> "outro tipo: " + obj.getClass().getSimpleName();
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Record Patterns — JEP 440 (GA Java 21)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Record patterns permitem <b>desestruturar</b> records diretamente no switch/instanceof.
     * Extrai componentes do record sem chamar getters explicitamente.
     */
    record Point(int x, int y) {}
    record Line(Point start, Point end) {}
    record Circle3D(Point center, double radius) {}

    sealed interface Geometry permits Point, Line, Circle3D {}

    static String describeGeometry(Object geo) {
        return switch (geo) {
            // Record pattern — desestrutura Point em x e y
            case Point(var x, var y) when x == 0 && y == 0
                -> "origem (0, 0)";

            case Point(var x, var y)
                -> "ponto em (%d, %d)".formatted(x, y);

            // Record pattern aninhado — desestrutura Line → Point → x, y
            case Line(Point(var x1, var y1), Point(var x2, var y2))
                -> "linha de (%d,%d) até (%d,%d), comprimento=%.2f"
                   .formatted(x1, y1, x2, y2, Math.hypot(x2 - x1, y2 - y1));

            // Record pattern com guard
            case Circle3D(Point(var cx, var cy), var r) when r <= 0
                -> "círculo inválido (raio ≤ 0)";

            case Circle3D(Point(var cx, var cy), var r)
                -> "círculo em (%d,%d) com raio %.1f, área=%.2f"
                   .formatted(cx, cy, r, Math.PI * r * r);

            default -> "geometria desconhecida";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Sealed Classes — Exaustividade garantida em compile time
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Com sealed classes, o compilador verifica em compile time que
     * todos os subtipos são cobertos — sem risco de NPE por caso esquecido.
     */
    sealed interface Result<T> permits Result.Ok, Result.Err {
        record Ok<T>(T value)   implements Result<T> {}
        record Err<T>(String message) implements Result<T> {}
    }

    static <T> String handleResult(Result<T> result) {
        return switch (result) {
            case Result.Ok<T>  ok  -> "sucesso: " + ok.value();
            case Result.Err<T> err -> "erro: " + err.message();
            // Sem default — sealed garante exaustividade
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== Pattern Matching for switch — Java 21 ===\n");

        // Type patterns
        System.out.println("--- Áreas ---");
        System.out.printf("Circle(5)    = %.2f%n", area(new Circle(5)));
        System.out.printf("Rectangle(3,4)= %.2f%n", area(new Rectangle(3, 4)));
        System.out.printf("Triangle(6,4) = %.2f%n", area(new Triangle(6, 4)));

        // Guarded patterns
        System.out.println("\n--- Classificação ---");
        System.out.println(classify(new Circle(200)));
        System.out.println(classify(new Circle(50)));
        System.out.println(classify(new Circle(5)));
        System.out.println(classify(new Rectangle(4, 4)));
        System.out.println(classify(new Rectangle(3, 5)));

        // null handling
        System.out.println("\n--- Null handling ---");
        System.out.println(describe(null));
        System.out.println(describe(-5));
        System.out.println(describe(42));
        System.out.println(describe("   "));
        System.out.println(describe("Java"));

        // Record patterns
        System.out.println("\n--- Record Patterns ---");
        System.out.println(describeGeometry(new Point(0, 0)));
        System.out.println(describeGeometry(new Point(3, 4)));
        System.out.println(describeGeometry(new Line(new Point(0, 0), new Point(3, 4))));
        System.out.println(describeGeometry(new Circle3D(new Point(1, 2), 5.0)));

        // Sealed Result type
        System.out.println("\n--- Sealed Result<T> ---");
        System.out.println(handleResult(new Result.Ok<>("dados carregados")));
        System.out.println(handleResult(new Result.Err<>("conexão recusada")));
    }
}
