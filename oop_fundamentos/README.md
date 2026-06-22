# Mod 13 — OOP: Interfaces, Classes Abstratas, Composição e Agregação

> **Conceito central:** Fundamentos de Orientação a Objetos aplicados a um CRUD de usuários.  
> `interface` · `abstract class` · herança · polimorfismo · default methods · `Optional`

---

## Conteúdo

```
mod13/mod13/
├── interfaces/
│   ├── entities/User.java
│   ├── repositories/
│   │   ├── UsersRepository.java         # Interface de contrato (sem estado)
│   │   └── UsersRepositoryInMemory.java # Implementação com List interna
│   ├── useCases/
│   │   ├── CreateUserUseCase.java
│   │   ├── FindByIdUserUseCase.java
│   │   ├── FindByNameUserUseCase.java
│   │   ├── FindAllUserUseCase.java
│   │   └── DeleteUserUseCase.java
│   └── Main.java
│
├── classes_abstratas/
│   ├── entities/
│   │   ├── Shape.java      # Classe abstrata com método abstrato area()
│   │   ├── Circle.java     # extends Shape
│   │   └── Rectangle.java  # extends Shape
│   ├── enums/Color.java
│   └── Main.java
│
└── exercicio/
    ├── entities/
    │   ├── Pessoa.java        # Classe base abstrata
    │   ├── PessoaFisica.java  # extends Pessoa — CPF
    │   └── PessoaJuridica.java# extends Pessoa — CNPJ
    └── Main.java
```

---

## Interface vs Classe Abstrata

| Critério | `interface` | `abstract class` |
|----------|-------------|------------------|
| Herança múltipla | ✅ (`implements A, B`) | ❌ (single inheritance) |
| Construtores | ❌ | ✅ |
| Estado (campos) | ❌ (apenas `static final`) | ✅ |
| `default` methods | ✅ (Java 8+) | ✅ |
| Quando usar | **Contrato** entre partes não relacionadas | **Comportamento base** de uma hierarquia |

---

## Repositório como Interface de Contrato

```java
// Interface — define O QUÊ, não o COMO
public interface UsersRepository {
    List<User> findAll();
    Optional<User> findById(Integer id);
    Optional<User> findByName(String name);
    void addUser(User user);
    void remove(User user);
}

// Implementação — define O COMO (estado interno encapsulado)
public class UsersRepositoryInMemory implements UsersRepository {
    private final List<User> users = new ArrayList<>();  // estado privado

    @Override
    public Optional<User> findById(Integer id) {
        if (id == null || id < 0 || id >= users.size()) return Optional.empty();
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users);  // cópia defensiva — imutabilidade externa
    }
}
```

**Benefício:** código cliente depende da interface, não da implementação.
Trocar para `UsersRepositoryDatabase` não afeta os use cases.

---

## Polimorfismo com Classes Abstratas

```java
// Classe abstrata — define contrato + comportamento comum
public abstract class Shape {
    protected Color color;

    public Shape(Color color) { this.color = color; }

    public abstract double area();  // cada subclasse implementa

    public String describe() {      // comportamento comum
        return "Shape[color=%s, area=%.2f]".formatted(color, area());
    }
}

// Subclasses
public class Circle extends Shape {
    private final double radius;
    @Override public double area() { return Math.PI * radius * radius; }
}

public class Rectangle extends Shape {
    private final double width, height;
    @Override public double area() { return width * height; }
}

// Polimorfismo em ação
List<Shape> shapes = List.of(new Circle(Color.RED, 5), new Rectangle(Color.BLUE, 3, 4));
shapes.forEach(s -> System.out.println(s.describe()));  // chama area() correto para cada tipo
```

---

## Composição vs Herança

```
Herança ("É um"):
  PessoaFisica   IS-A  Pessoa  ✅ (relação semântica clara)
  Rectangle      IS-A  Shape   ✅

Composição ("Tem um"):
  Cliente        HAS-A Endereço  ✅ (preferível a EnderecoCliente extends Cliente)
  Pedido         HAS-A List<Item>

Regra: prefira composição sobre herança quando a relação não é
claramente IS-A, ou quando precisar de flexibilidade em runtime.
```

---

## Anti-pattern corrigido — Interface com estado

O código original tinha `ArrayList<User> users = new ArrayList<>()` dentro da interface.
Campos em interfaces são implicitamente `public static final` — o `ArrayList` era
compartilhado entre **todas as implementações**, tornando o estado global mutável.

**Solução aplicada:** interface define apenas contrato; estado encapsulado na implementação.

---

## Próximo Módulo

[mod14\_projeto01 — Projeto 01: CRUD de Clientes com Swing](../mod14_projeto01/README.md)
