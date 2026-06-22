# programacao_funcional — Programação Funcional em Java

> Lambdas, interfaces funcionais e a Streams API formam o pilar funcional da JDK 8+.
> Juntos, permitem escrever pipelines declarativos, composáveis e paralelizáveis.

---

## Submódulos

| Diretório | Conteúdo |
|---|---|
| [`lambdas/`](./lambdas/README.md) | Sintaxe de lambda, `java.util.function` (`Supplier`, `Consumer`, `Function`, `Predicate`, `BiFunction`), method references, closures, `@FunctionalInterface` |
| [`streams/`](./streams/README.md) | Pipeline lazy (source → intermediate ops → terminal), `flatMap`, `Collectors` avançados (`groupingBy`, `partitioningBy`), parallel streams |

---

## A relação entre lambdas e streams

```
Lambda        = função anônima passada como valor
Stream        = pipeline de operações sobre sequências
                cada operação recebe uma lambda como argumento

Exemplo completo:
  customers.stream()                          // source
    .filter(c -> c.isPremium())               // lambda: Predicate<Customer>
    .map(Customer::email)                     // method reference: Function<Customer,String>
    .sorted()                                 // operação intermediária sem lambda
    .collect(Collectors.toList())             // lambda interna em Collectors
    ↑                                         ↑
  StreamsAPI                              Funcional Interface
```

---

## Interfaces funcionais essenciais

| Interface | Assinatura | Uso |
|---|---|---|
| `Supplier<T>` | `() → T` | Lazy creation, factories |
| `Consumer<T>` | `T → void` | `forEach`, side effects |
| `Function<T,R>` | `T → R` | `map`, transformações |
| `Predicate<T>` | `T → boolean` | `filter`, validações |
| `BiFunction<T,U,R>` | `(T,U) → R` | `reduce`, merges |
| `UnaryOperator<T>` | `T → T` | `replaceAll`, identity transforms |

---

## Complexidade em Streams

| Operação | Complexidade |
|---|---|
| `filter` | O(n) |
| `map` | O(n) |
| `sorted` | O(n log n) |
| `distinct` | O(n) com HashSet interno |
| `collect(toList())` | O(n) |
| `Stream.toList()` Java 16+ | O(n) — retorna imutável |

Pipeline lazy: operações intermediárias só executam quando uma operação terminal é chamada.
