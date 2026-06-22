# Mod 20/21 — Lambda Expressions e Interfaces Funcionais

> **Conceito central:** Programação funcional em Java com funções como cidadãs de primeira classe.  
> `java.util.function` · Method References · Closures · Functional Interface · `@FunctionalInterface`

---

## Conteúdo

```
mod20_21/src/lambdas/
├── anonimas/
│   ├── MetodosComLambdas.java   # Comparator, Runnable, EventListener via lambda
│   └── FunctionalInterfaces.java # Function, Predicate, Consumer, Supplier, BiFunction
└── exercicio/
    └── Main.java                # Problemas práticos com lambdas
```

---

## O que são Lambdas

Lambda expression é uma função anônima — sem nome, sem classe — que pode ser passada como argumento ou armazenada em variável.

```java
// Antes de Java 8 — classe anônima verbosa
Comparator<String> comp = new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.compareTo(b);
    }
};

// Java 8+ — lambda concisa
Comparator<String> comp = (a, b) -> a.compareTo(b);

// Ainda mais conciso — method reference
Comparator<String> comp = String::compareTo;
```

---

## Interfaces Funcionais — `java.util.function`

Uma **interface funcional** tem exatamente **um método abstrato**. É o target de lambdas e method references.

| Interface | Assinatura | Uso |
|-----------|-----------|-----|
| `Supplier<T>` | `T get()` | Produz um valor sem input |
| `Consumer<T>` | `void accept(T)` | Consome sem retornar |
| `Function<T,R>` | `R apply(T)` | Transforma T em R |
| `Predicate<T>` | `boolean test(T)` | Testa uma condição |
| `BiFunction<T,U,R>` | `R apply(T, U)` | Transforma dois valores em um |
| `UnaryOperator<T>` | `T apply(T)` | `Function<T,T>` especializada |
| `BinaryOperator<T>` | `T apply(T, T)` | `BiFunction<T,T,T>` especializada |
| `Runnable` | `void run()` | Execução sem input/output |

```java
// Supplier — produz sem receber
Supplier<List<String>> listFactory = ArrayList::new;
List<String> list = listFactory.get();

// Consumer — consome sem retornar
Consumer<String> printer  = System.out::println;
Consumer<String> upperLog = s -> System.out.println(s.toUpperCase());
printer.andThen(upperLog).accept("hello");  // composição de Consumers

// Function — transforma
Function<String, Integer> length  = String::length;
Function<String, String>  toUpper = String::toUpperCase;
Function<String, String>  combined = toUpper.andThen(s -> "[" + s + "]");
combined.apply("java");  // "[JAVA]"

// Predicate — testa condição
Predicate<String> isBlank    = String::isBlank;
Predicate<String> isLong     = s -> s.length() > 10;
Predicate<String> isLongText = isBlank.negate().and(isLong);

// BiFunction — dois inputs
BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
repeat.apply("Java ", 3);  // "Java Java Java "
```

---

## Method References

Atalho para lambdas que apenas chamam um método existente:

```java
// Tipo               Sintaxe              Equivalente lambda
// Estático           Classe::metodo       x -> Classe.metodo(x)
// De instância (any) Classe::metodo       (obj, x) -> obj.metodo(x)
// De instância (fixo)instancia::metodo    x -> instancia.metodo(x)
// Construtor         Classe::new          args -> new Classe(args)

// Exemplos
Function<String, Integer> parse = Integer::parseInt;   // estático
Consumer<String> print    = System.out::println;        // instância fixa
Function<String, String> trim = String::strip;          // instância any
Supplier<List<String>> newList = ArrayList::new;        // construtor
```

---

## Closures — Capturando Variáveis Externas

```java
// Lambda captura variáveis "effectively final" do escopo externo
String prefix = "Hello, ";  // effectively final (nunca reatribuída)

Function<String, String> greet = name -> prefix + name;
greet.apply("Java");  // "Hello, Java"

// ❌ Não pode capturar variável mutável
int count = 0;
Runnable r = () -> count++;  // Erro de compilação!

// ✅ Use array ou AtomicInteger para estado mutável em lambda
int[] counter = {0};
Runnable r = () -> counter[0]++;  // funciona (array é effectively final)
```

---

## Composição Funcional

```java
// Function.compose(f) — aplica f antes
// Function.andThen(f) — aplica f depois

Function<Integer, Integer> times2  = x -> x * 2;
Function<Integer, Integer> plus10  = x -> x + 10;

Function<Integer, Integer> first2then10 = times2.andThen(plus10);
first2then10.apply(5);  // (5*2) + 10 = 20

Function<Integer, Integer> first10then2 = times2.compose(plus10);
first10then2.apply(5);  // (5+10) * 2 = 30

// Predicate.and(), or(), negate()
Predicate<Integer> isPositive = n -> n > 0;
Predicate<Integer> isEven     = n -> n % 2 == 0;
Predicate<Integer> isPosEven  = isPositive.and(isEven);

List.of(-2, -1, 0, 1, 2, 3, 4)
    .stream()
    .filter(isPosEven)
    .toList();  // [2, 4]
```

---

## Criando sua própria Interface Funcional

```java
// @FunctionalInterface garante erro de compilação se adicionar 2o método abstrato
@FunctionalInterface
public interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}

TriFunction<String, Integer, Boolean, String> format =
    (text, times, upper) -> {
        String result = text.repeat(times);
        return upper ? result.toUpperCase() : result;
    };

format.apply("java ", 3, true);  // "JAVA JAVA JAVA "
```

---

## Próximo Módulo

[mod22 — Streams API](../mod22/README.md)
