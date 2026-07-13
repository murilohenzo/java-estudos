package br.com.murilo.constructors;

/**
 * Demonstração de <b>Flexible Constructor Bodies</b> — JEP 513 (GA no Java 25).
 *
 * <p>Permite statements antes de {@code super(...)} / {@code this(...)}.
 * Não se pode usar o objeto sob construção como receptor de métodos de instância
 * antes do super, mas é permitido validar args e inicializar campos da subclasse.
 */
public class FlexibleConstructorsDemo {

    public static void main(String[] args) {
        System.out.println("=== Flexible Constructor Bodies — Java 25 ===\n");

        Employee ok = new Employee("Ana", 30, "ENG-01");
        System.out.println("Criado: " + ok);

        try {
            new Employee("Bob", 16, "ENG-02");
        } catch (IllegalArgumentException e) {
            System.out.println("Fail-fast (esperado): " + e.getMessage());
        }

        Square square = new Square(4.0);
        System.out.println("Square area=" + square.area() + " side=" + square.side());
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Exemplo 1: validar antes do super (fail-fast)
// ─────────────────────────────────────────────────────────────────────────────

class Person {
    final String name;
    final int age;

    Person(String name, int age) {
        if (age < 0) throw new IllegalArgumentException("age < 0");
        this.name = name;
        this.age = age;
        // Em designs reais, evitar chamar métodos overridable aqui —
        // o ponto do JEP 513 é permitir a subclasse preparar estado antes.
        describe();
    }

    void describe() {
        System.out.println("  Person construído: " + name + ", " + age + " anos");
    }
}

class Employee extends Person {
    final String officeId;

    Employee(String name, int age, String officeId) {
        // ✅ código ANTES do super — validação e init de campo
        if (age < 18 || age > 67) {
            throw new IllegalArgumentException("idade de empregado inválida: " + age);
        }
        if (officeId == null || officeId.isBlank()) {
            throw new IllegalArgumentException("officeId obrigatório");
        }
        this.officeId = officeId; // campo da subclasse pronto antes do super
        super(name, age);
    }

    @Override
    void describe() {
        // officeId já inicializado — sem ver null/default
        System.out.println("  Employee: " + name + ", office=" + officeId);
    }

    @Override
    public String toString() {
        return "Employee{name='%s', age=%d, officeId='%s'}".formatted(name, age, officeId);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Exemplo 2: preparar argumentos do super
// ─────────────────────────────────────────────────────────────────────────────

class Shape {
    final double area;

    Shape(double area) {
        if (area <= 0) throw new IllegalArgumentException("area <= 0");
        this.area = area;
    }

    double area() {
        return area;
    }
}

class Square extends Shape {
    final double side;

    Square(double side) {
        // cálculos / normalização antes de invocar o construtor da superclasse
        if (side <= 0) throw new IllegalArgumentException("side <= 0");
        double computedArea = side * side;
        this.side = side;
        super(computedArea);
    }

    double side() {
        return side;
    }
}
