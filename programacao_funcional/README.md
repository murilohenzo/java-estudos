# programacao_funcional — Programação Funcional em Java

> Lambdas, interfaces funcionais e a Streams API formam o pilar funcional da JDK 8+.  
> Juntos, permitem pipelines declarativos, composáveis e (quando faz sentido) paralelizáveis.

---

## Submódulos

| Diretório | Conteúdo |
|---|---|
| [`lambdas/`](./lambdas/README.md) | Sintaxe, catálogo completo de `java.util.function`, method references, closures, composição, interfaces customizadas |
| [`streams/`](./streams/README.md) | Pipeline lazy, `flatMap`, `Collectors`, parallel streams |

---

## Mapa mental

```
Programação funcional em Java
│
├── Lambda          → função anônima (valor)
├── Method ref      → atalho para método existente
├── Functional IF   → contrato de 1 método abstrato (target type)
└── Stream          → pipeline que consome essas funções
        │
        ├── filter(Predicate)
        ├── map(Function)
        ├── forEach(Consumer)
        ├── reduce(BinaryOperator)
        └── collect(Collector)  ← Collector usa várias FIs internamente
```

Exemplo ponta a ponta:

```java
customers.stream()
    .filter(Customer::isPremium)          // Predicate<Customer>
    .map(Customer::email)                 // Function<Customer, String>
    .map(String::toLowerCase)             // Function<String, String>
    .sorted()                             // Comparable
    .forEach(System.out::println);        // Consumer<String>
```

---

## Famílias de interfaces funcionais

O pacote `java.util.function` organiza-se por **aridade** e por **especialização primitiva** (evita boxing).

### Núcleo (objetos)

| Interface | Assinatura | Caso de uso típico |
|---|---|---|
| `Supplier<T>` | `() → T` | Factory lazy, default value, `Optional.orElseGet` |
| `Consumer<T>` | `T → void` | `forEach`, logging, side-effect controlado |
| `Function<T,R>` | `T → R` | `map`, DTO mapping, parsing |
| `Predicate<T>` | `T → boolean` | `filter`, validação, guards |
| `UnaryOperator<T>` | `T → T` | `List.replaceAll`, transformações in-place tipadas |
| `BiFunction<T,U,R>` | `(T,U) → R` | Merge, `Map.merge`, cálculos com 2 inputs |
| `BinaryOperator<T>` | `(T,T) → T` | `reduce`, `Collectors.reducing` |
| `BiConsumer<T,U>` | `(T,U) → void` | `Map.forEach`, callbacks com 2 args |
| `BiPredicate<T,U>` | `(T,U) → boolean` | Comparação / regra com 2 entradas |

### Primitivas (sem boxing)

| Interface | Assinatura | Quando preferir |
|---|---|---|
| `IntSupplier` / `LongSupplier` / `DoubleSupplier` | `() → primitivo` | Geradores numéricos |
| `IntConsumer` / `LongConsumer` / `DoubleConsumer` | `primitivo → void` | `IntStream.forEach` |
| `IntPredicate` / … | `primitivo → boolean` | `IntStream.filter` |
| `IntFunction<R>` / `ToIntFunction<T>` | `int → R` / `T → int` | `map` / `mapToInt` |
| `IntUnaryOperator` / `IntBinaryOperator` | `int → int` / `(int,int) → int` | Aritmética em streams primitivos |
| `ObjIntConsumer<T>` | `(T,int) → void` | Indexação + objeto (`forEach` com índice manual) |
| `BooleanSupplier` | `() → boolean` | Feature flags / condições lazy |

Detalhamento com exemplos: [`lambdas/README.md`](./lambdas/README.md).

---

## Complexidade em Streams

| Operação | Complexidade |
|---|---|
| `filter` / `map` / `forEach` | O(n) |
| `sorted` | O(n log n) |
| `distinct` | O(n) (HashSet interno) |
| `collect(toList())` / `Stream.toList()` | O(n) — `toList()` (16+) retorna imutável |

Pipeline **lazy**: intermediárias só rodam quando uma terminal é chamada.

---

## Demos (código)

```
programacao_funcional/
├── lambdas/src/lambdas/
│   ├── FunctionalInterfacesCatalog.java   # núcleo + Bi* + composição
│   ├── PrimitiveFunctionalDemo.java       # especializações int/long/double
│   ├── UseCasesDemo.java                  # validação, retry, pipeline de domínio
│   └── anonimas/ …                        # exemplos iniciais
└── streams/src/exercicio/
    └── Demo.java
```

```bash
cd programacao_funcional/lambdas/src
javac lambdas/FunctionalInterfacesCatalog.java
java lambdas.FunctionalInterfacesCatalog

javac lambdas/PrimitiveFunctionalDemo.java
java lambdas.PrimitiveFunctionalDemo

javac lambdas/UseCasesDemo.java
java lambdas.UseCasesDemo
```
