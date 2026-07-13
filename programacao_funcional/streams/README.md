# Streams API

> **Conceito central:** pipeline declarativo sobre sequências.  
> Cada operação intermediária/terminal recebe uma interface funcional — ver [`../lambdas/README.md`](../lambdas/README.md).

---

## Conteúdo

```
streams/src/exercicio/
├── Customer.java
└── Demo.java
```

---

## Anatomia de um Stream Pipeline

```
Source → Intermediate Operations → Terminal Operation
  │               │                       │
List.of(...)   .filter(Predicate)      .collect(...)
               .map(Function)          .toList()
               .sorted(Comparator)     .reduce(BinaryOperator)
               .flatMap(Function)      .forEach(Consumer)
               .peek(Consumer)         .anyMatch(Predicate)
```

Streams são **lazy** — intermediárias não executam até uma terminal.

### Qual interface entra onde

| Operação | Interface funcional |
|---|---|
| `filter` / `takeWhile` | `Predicate<T>` |
| `map` | `Function<T,R>` |
| `mapToInt` | `ToIntFunction<T>` |
| `flatMap` | `Function<T, Stream<R>>` |
| `peek` / `forEach` | `Consumer<T>` |
| `reduce` | `BinaryOperator<T>` ou `BiFunction` |
| `anyMatch` / `allMatch` | `Predicate<T>` |
| `IntStream.map` | `IntUnaryOperator` |

---

## Operações Intermediárias

```java
List<String> names = List.of("Ana", "Bob", "Carlos", "Ana", "Diana", "Bo");

// filter — seleciona elementos que satisfazem o predicado
names.stream()
     .filter(n -> n.length() > 3)
     .toList();  // ["Carlos", "Diana"]

// map — transforma cada elemento
names.stream()
     .map(String::toUpperCase)
     .toList();  // ["ANA", "BOB", "CARLOS", "ANA", "DIANA", "BO"]

// distinct — remove duplicatas (usa equals/hashCode)
names.stream()
     .distinct()
     .toList();  // ["Ana", "Bob", "Carlos", "Diana", "Bo"]

// sorted — ordena (natural ou com Comparator)
names.stream()
     .sorted()
     .toList();  // ["Ana", "Ana", "Bo", "Bob", "Carlos", "Diana"]

names.stream()
     .sorted(Comparator.comparing(String::length).reversed())
     .toList();  // ["Carlos", "Diana", "Ana", "Ana", "Bob", "Bo"]

// limit / skip — paginação simples
names.stream().skip(2).limit(3).toList();  // ["Carlos", "Ana", "Diana"]

// peek — debug sem modificar o stream
names.stream()
     .peek(n -> System.out.println("Before: " + n))
     .filter(n -> n.startsWith("A"))
     .peek(n -> System.out.println("After: " + n))
     .toList();
```

---

## flatMap — Achatando Streams aninhados

```java
// Problema: cada usuário tem uma lista de emails
List<List<String>> emailLists = List.of(
    List.of("ana@a.com", "ana@b.com"),
    List.of("bob@c.com"),
    List.of("carlos@d.com", "carlos@e.com", "carlos@f.com")
);

// map + flatMap
List<String> allEmails = emailLists.stream()
    .flatMap(List::stream)  // List<List<String>> → Stream<String>
    .distinct()
    .sorted()
    .toList();

// Exemplo com palavras de frases
List<String> sentences = List.of("java is great", "streams are powerful");
List<String> words = sentences.stream()
    .flatMap(s -> Arrays.stream(s.split(" ")))
    .distinct()
    .sorted()
    .toList();
```

---

## Operações Terminais

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// collect — materializa o stream em coleção
List<Integer>  list  = numbers.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());
Set<Integer>   set   = numbers.stream().collect(Collectors.toSet());
String         joined= numbers.stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
// "[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"

// toList() — Java 16+ (imutável)
List<Integer> evens = numbers.stream().filter(n -> n % 2 == 0).toList();

// reduce — agrega elementos em um único valor
int sum     = numbers.stream().reduce(0, Integer::sum);            // 55
int product = numbers.stream().reduce(1, (a, b) -> a * b);        // 3628800
Optional<Integer> max = numbers.stream().reduce(Integer::max);     // 10

// count, sum, average, min, max
long   count = numbers.stream().filter(n -> n > 5).count();          // 5
int    sum2  = numbers.stream().mapToInt(Integer::intValue).sum();   // 55
double avg   = numbers.stream().mapToDouble(Integer::doubleValue).average().orElse(0); // 5.5
Optional<Integer> min = numbers.stream().min(Comparator.naturalOrder()); // 1

// forEach — efeito colateral
numbers.stream().filter(n -> n % 3 == 0).forEach(System.out::println); // 3, 6, 9

// findFirst / findAny
Optional<Integer> first = numbers.stream().filter(n -> n > 5).findFirst(); // 6
boolean anyMatch = numbers.stream().anyMatch(n -> n > 9);  // true
boolean allMatch = numbers.stream().allMatch(n -> n > 0);  // true
boolean noneMatch= numbers.stream().noneMatch(n -> n < 0); // true
```

---

## Collectors Avançados

```java
record Person(String name, String city, int age) {}

List<Person> people = List.of(
    new Person("Ana",    "SP", 25),
    new Person("Bob",    "RJ", 30),
    new Person("Carlos", "SP", 35),
    new Person("Diana",  "RJ", 28)
);

// groupingBy — agrupa por campo
Map<String, List<Person>> byCity = people.stream()
    .collect(Collectors.groupingBy(Person::city));
// {"SP": [Ana, Carlos], "RJ": [Bob, Diana]}

// groupingBy + counting
Map<String, Long> countByCity = people.stream()
    .collect(Collectors.groupingBy(Person::city, Collectors.counting()));
// {"SP": 2, "RJ": 2}

// groupingBy + averagingInt
Map<String, Double> avgAgeByCity = people.stream()
    .collect(Collectors.groupingBy(Person::city, Collectors.averagingInt(Person::age)));

// partitioningBy — divide em true/false
Map<Boolean, List<Person>> partitioned = people.stream()
    .collect(Collectors.partitioningBy(p -> p.age() >= 30));
// {true: [Bob, Carlos], false: [Ana, Diana]}

// toMap
Map<String, Integer> nameToAge = people.stream()
    .collect(Collectors.toMap(Person::name, Person::age));
```

---

## Streams Paralelos

```java
// Paraleliza automaticamente usando ForkJoinPool.commonPool()
long sum = LongStream.rangeClosed(1, 1_000_000)
    .parallel()
    .filter(n -> n % 2 == 0)
    .sum();

// Quando usar paralelo:
// ✅ Coleções grandes (>10k elementos)
// ✅ Operações CPU-intensivas sem I/O
// ❌ Operações com estado compartilhado (thread-safety)
// ❌ Coleções pequenas (overhead de coordenação supera ganho)
```

---

## Relacionado

- [Lambdas e Interfaces Funcionais](../lambdas/README.md)
- [Testes](../../testes/README.md)
