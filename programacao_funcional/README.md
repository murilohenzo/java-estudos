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

## Paradigma OO vs funcional

Java é multiparadigma: objetos modelam **estado + comportamento encapsulado**; o estilo funcional enfatiza **funções como valores**, transformação de dados e composição — especialmente útil em pipelines (`Stream`, `Function.andThen`, `Predicate.and`).

| Dimensão | Orientação a Objetos | Funcional |
|---|---|---|
| Unidade central | Objeto (estado + métodos) | Função / expressão |
| Estado | Mutável por padrão (campos) | Prefere imutabilidade |
| Efeitos colaterais | Comuns (setters, I/O no método) | Isolados / explícitos |
| Reuso | Herança e polimorfismo | Composição de funções |
| Extensão típica | Subclasse / override | Nova função encadeada |
| Exemplo mental | `pedido.calcularTotal()` | `calcularTotal(pedido)` ou `Pedido::calcularTotal` como `Function` |

### Onde cada um brilha

| Preferir OO | Preferir funcional |
|---|---|
| Modelo de domínio rico (invariantes, ciclo de vida) | Transformar coleções (filter/map/reduce) |
| Encapsular regras que mudam juntas no mesmo agregado | Regras pequenas e compostáveis (`Predicate`, `Function`) |
| APIs com identidade e colaborações (services, repositories) | Pipelines declarativos, parallel streams, callbacks |

Na prática Java mistura os dois: um `OrderService` OO orquestra; por dentro usa `stream().filter(...).map(...)`.

```java
// OO: comportamento no tipo
record Order(List<Item> items) {
    BigDecimal total() {
        return items.stream()                          // funcional
                .map(Item::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

### Composição vs herança (ponte entre os paradigmas)

- **Herança (OO clássica):** “é um” — reuso por especialização de classe.
- **Composição (OO moderna + FP):** “tem um” / “aplica f depois g” — reuso por montagem.

O estilo funcional empurra para composição (`f.andThen(g)`, Strategy, Decorator funcional). Isso evita hierarquias profundas e o problema abaixo.

Detalhe de design: [Composição vs Herança](../guia_engenharia/COESAO_ACOPLAMENTO.md#composição-vs-herança).

---

## O problema do diamante

O **diamond problem** (herança em diamante) surge com **herança múltipla de implementação**: duas superclasses definem o mesmo método; a subclasse não sabe qual herdar.

```
        A.speak()
         /    \
    B.speak()  C.speak()   ← ambas sobrescrevem / herdaram de A
         \    /
        D.speak()          ← qual versão? AMBÍGUO
```

### Por que é um problema

1. **Ambiguidade semântica** — `D` chama `speak()`: usa a de `B` ou a de `C`?
2. **Duplicação de estado** — se `A` tem campos, `D` pode “herdar” duas cópias via `B` e `C` (problema clássico em C++).
3. **Fragilidade** — mudar `A`/`B`/`C` quebra subclasses distantes (acoplamento alto).

### Java: classes vs interfaces

Java **proíbe herança múltipla de classes** (só `extends` uma classe) — elimina o diamante clássico de implementação/estado.

Interfaces permitem múltiplos `implements`, e desde Java 8 têm `default` methods. Se duas interfaces trazem o **mesmo** default, a classe concreta **deve** sobrescrever e desambiguar:

```java
interface Fly { default String move() { return "fly"; } }
interface Swim { default String move() { return "swim"; } }

// ❌ não compila — conflito de defaults (diamante de interface)
// class Duck implements Fly, Swim {}

// ✅ desambiguação explícita
class Duck implements Fly, Swim {
    @Override
    public String move() {
        return Fly.super.move() + " & " + Swim.super.move();
    }
}
```

📄 **Demo:** `lambdas/src/lambdas/ParadigmsAndDiamondDemo.java`

### Como o estilo funcional evita o diamante

Em vez de hierarquia `A → B/C → D`, monte comportamentos como **funções compostas**:

```java
Function<String, String> fly  = s -> s + "→fly";
Function<String, String> swim = s -> s + "→swim";

// composição explícita — sem ambiguidade de herança
Function<String, String> duck = fly.andThen(swim);
duck.apply("duck");  // "duck→fly→swim"
```

Ou Strategy/Decorator por composição de objetos (sem árvore de classes em diamante).

| Abordagem | Resolve diamante? |
|---|---|
| Herança múltipla de classes | ❌ (Java nem permite) |
| Interfaces + `default` em conflito | ⚠️ só com override explícito |
| Composição de objetos | ✅ |
| Composição de funções | ✅ |

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
│   ├── ParadigmsAndDiamondDemo.java       # OO vs FP + diamante de defaults
│   ├── FunctionalInterfacesCatalog.java   # núcleo + Bi* + composição
│   ├── PrimitiveFunctionalDemo.java       # especializações int/long/double
│   ├── UseCasesDemo.java                  # validação, retry, pipeline de domínio
│   └── anonimas/ …                        # exemplos iniciais
└── streams/src/exercicio/
    └── Demo.java
```

```bash
cd programacao_funcional/lambdas/src
javac lambdas/ParadigmsAndDiamondDemo.java
java lambdas.ParadigmsAndDiamondDemo

javac lambdas/FunctionalInterfacesCatalog.java
java lambdas.FunctionalInterfacesCatalog

javac lambdas/PrimitiveFunctionalDemo.java
java lambdas.PrimitiveFunctionalDemo

javac lambdas/UseCasesDemo.java
java lambdas.UseCasesDemo
```
