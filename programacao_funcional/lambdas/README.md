# Lambdas e Interfaces Funcionais

> **Conceito central:** funções como valores — passadas, compostas e reutilizadas.  
> `java.util.function` · Method References · Closures · `@FunctionalInterface` · Casos de uso

---

## Conteúdo

```
lambdas/src/lambdas/
├── FunctionalInterfacesCatalog.java  # Supplier → BiPredicate + composição
├── PrimitiveFunctionalDemo.java      # Int*/Long*/Double*/BooleanSupplier
├── UseCasesDemo.java                 # validação, retry, Map.merge, pipeline
├── IntBinaryOperatorClass.java       # implementação clássica (pré-lambda)
├── TesteLambdas.java / TesteLambdasThreads.java
└── anonimas/                         # sintaxe e method references
```

---

## O que é uma Lambda

Função anônima — sem nome de método/classe — que o compilador converte no único método abstrato de uma **interface funcional** (target type).

```java
// Antes de Java 8 — classe anônima
Comparator<String> comp = new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.compareTo(b);
    }
};

// Lambda
Comparator<String> comp = (a, b) -> a.compareTo(b);

// Method reference
Comparator<String> comp = String::compareTo;
```

### Regras de sintaxe (resumo)

| Forma | Exemplo |
|---|---|
| Sem params | `() -> "ok"` |
| Um param (parens opcional) | `s -> s.length()` / `(s) -> s.length()` |
| Vários params | `(a, b) -> a + b` |
| Bloco com return | `x -> { var y = x * 2; return y; }` |
| Tipos explícitos | `(String a, String b) -> a.compareTo(b)` |

---

## Interface funcional

Exatamente **um** método abstrato (SAM). Métodos `default` / `static` não contam.

```java
@FunctionalInterface  // opcional, mas recomendado — falha se houver 2 abstratos
public interface Predicate<T> {
    boolean test(T t);

    default Predicate<T> and(Predicate<? super T> other) { ... }
    default Predicate<T> negate() { ... }
    static <T> Predicate<T> isEqual(Object target) { ... }
}
```

---

## Catálogo — núcleo (`java.util.function`)

### 1. `Supplier<T>` — `() → T`

Produz valor **sem input**. Lazy evaluation.

```java
Supplier<LocalDateTime> now = LocalDateTime::now;
Supplier<List<String>> empty = ArrayList::new;

Optional.ofNullable(findUser(id))
    .orElseGet(() -> createGuest());   // só cria se estiver vazio
```

| Caso de uso | Por quê Supplier |
|---|---|
| Factory / object pool | Adia criação até `get()` |
| Default em Optional | Evita custo se valor existir |
| Config lazy | Carrega propriedade só na 1ª leitura |

---

### 2. `Consumer<T>` — `T → void`

Consome sem retornar. Side effects.

```java
Consumer<String> log = System.out::println;
Consumer<String> audit = s -> auditTrail.append(s);

log.andThen(audit).accept("pedido#42");  // composição sequencial
```

| Caso de uso | Exemplo |
|---|---|
| `Iterable.forEach` | `list.forEach(this::save)` |
| Callbacks | `onSuccess.accept(result)` |
| Pipeline de efeitos | `validate.andThen(persist).andThen(notify)` |

---

### 3. `Function<T,R>` — `T → R`

Transformação pura (idealmente sem side effect).

```java
Function<String, Integer> len = String::length;
Function<String, String> upper = String::toUpperCase;

Function<String, String> tagged = upper.andThen(s -> "[" + s + "]");
tagged.apply("java");  // "[JAVA]"

Function<String, Integer> lenOfUpper = upper.andThen(len);
// compose: f.compose(g) ≡ f(g(x)) — g primeiro
```

| Caso de uso | Exemplo |
|---|---|
| `Stream.map` | `users.stream().map(User::dto)` |
| Parsing | `Function<String, Integer> parse = Integer::valueOf` |
| Adapter de camadas | `Entity → Response` |

---

### 4. `Predicate<T>` — `T → boolean`

Teste booleano composável.

```java
Predicate<String> blank = String::isBlank;
Predicate<String> longText = s -> s.length() > 10;
Predicate<String> usable = blank.negate().and(longText);

List.of("", "hi", "texto bem longo")
    .stream().filter(usable).toList();
```

Métodos úteis: `and`, `or`, `negate`, `Predicate.not`, `Predicate.isEqual`.

| Caso de uso | Exemplo |
|---|---|
| `Stream.filter` | `orders.filter(Order::isPaid)` |
| Validação | `rules.stream().allMatch(r -> r.test(dto))` |
| Feature toggle | `Predicate<User> canSeeBeta = u -> flags.enabled("beta", u)` |

---

### 5. `UnaryOperator<T>` — `T → T`

`Function<T,T>` especializada (mesma entrada/saída).

```java
UnaryOperator<String> trimUpper = s -> s.strip().toUpperCase();
List<String> names = new ArrayList<>(List.of(" ana ", "bob "));
names.replaceAll(trimUpper);  // API de List pede UnaryOperator
```

---

### 6. `BiFunction<T,U,R>` — `(T,U) → R`

```java
BiFunction<String, Integer, String> pad =
    (text, width) -> String.format("%" + width + "s", text);

BiFunction<BigDecimal, BigDecimal, BigDecimal> sum = BigDecimal::add;
```

| Caso de uso | Exemplo |
|---|---|
| `Map.merge` | `map.merge(key, 1, Integer::sum)` |
| Cálculo com 2 fontes | preço × quantidade |
| `reduce` com identidade tipada diferente | ver `Stream.reduce` 3-arg |

---

### 7. `BinaryOperator<T>` — `(T,T) → T`

`BiFunction<T,T,T>`. Agregações homogêneas.

```java
BinaryOperator<Integer> max = BinaryOperator.maxBy(Comparator.naturalOrder());
int biggest = Stream.of(3, 1, 4).reduce(max).orElseThrow();

BinaryOperator<String> join = (a, b) -> a + "," + b;
```

---

### 8. `BiConsumer<T,U>` — `(T,U) → void`

```java
BiConsumer<String, Integer> printScore =
    (name, score) -> System.out.println(name + "=" + score);

Map.of("Ana", 10, "Bob", 8).forEach(printScore);
```

---

### 9. `BiPredicate<T,U>` — `(T,U) → boolean`

```java
BiPredicate<String, String> sameIgnoreCase =
    (a, b) -> a.equalsIgnoreCase(b);

BiPredicate<User, Permission> allowed =
    (user, perm) -> user.roles().contains(perm.role());
```

---

## Especializações primitivas

Evita boxing (`Integer` ↔ `int`) em hot paths e em `IntStream` / `LongStream` / `DoubleStream`.

📄 **Demo:** `PrimitiveFunctionalDemo.java`

| Interface | Método | Uso |
|---|---|---|
| `IntSupplier` | `getAsInt()` | seeds, IDs, random |
| `IntConsumer` | `accept(int)` | `IntStream.forEach` |
| `IntPredicate` | `test(int)` | `IntStream.filter` |
| `IntFunction<R>` | `apply(int)` | `int → objeto` |
| `ToIntFunction<T>` | `applyAsInt(T)` | `mapToInt` |
| `IntUnaryOperator` | `applyAsInt(int)` | `IntStream.map` |
| `IntBinaryOperator` | `applyAsInt(int,int)` | `IntStream.reduce` |
| `ObjIntConsumer<T>` | `accept(T,int)` | objeto + índice |
| `BooleanSupplier` | `getAsBoolean()` | condição lazy / flag |

Há o trio equivalente para `long` e `double` (`Long*`, `Double*`, `ToLongFunction`, `ObjLongConsumer`, …).

```java
ToIntFunction<String> len = String::length;
IntBinaryOperator add = Integer::sum;

int total = Stream.of("a", "bbb", "cc")
    .mapToInt(len)
    .reduce(0, add);  // 6
```

---

## Method References

| Tipo | Sintaxe | Equivalente |
|---|---|---|
| Estático | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Instância (qualquer) | `String::strip` | `s -> s.strip()` |
| Instância (fixo) | `System.out::println` | `s -> System.out.println(s)` |
| Construtor | `ArrayList::new` | `() -> new ArrayList<>()` |
| Array | `String[]::new` | `n -> new String[n]` |

---

## Closures — captura de variáveis

A lambda só captura variáveis **effectively final** do escopo externo.

```java
String prefix = "Hi, ";
Function<String, String> greet = name -> prefix + name;  // ok

int count = 0;
// Runnable r = () -> count++;  // ❌ não compila

AtomicInteger counter = new AtomicInteger();
Runnable r = counter::incrementAndGet;  // ✅ objeto mutable, referência final
```

---

## Composição — montar regras maiores

```java
Function<Integer, Integer> times2 = x -> x * 2;
Function<Integer, Integer> plus10 = x -> x + 10;

times2.andThen(plus10).apply(5);   // 20
times2.compose(plus10).apply(5);   // 30

Predicate<Integer> positive = n -> n > 0;
Predicate<Integer> even = n -> n % 2 == 0;
positive.and(even).test(4);        // true

Consumer<String> a = System.out::println;
Consumer<String> b = s -> System.err.println("ERR:" + s);
a.andThen(b).accept("x");
```

---

## Interface funcional customizada

```java
@FunctionalInterface
public interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}

TriFunction<String, Integer, Boolean, String> format =
    (text, times, upper) -> {
        String r = text.repeat(times);
        return upper ? r.toUpperCase() : r;
    };
```

Use custom só quando as FIs da JDK não cabem (3+ args tipados, domínio específico).

---

## Casos de uso reunidos

📄 **Demo:** `UseCasesDemo.java`

1. **Validação composável** — lista de `Predicate<Pedido>`  
2. **Retry com `Supplier`** — tenta `get()` até sucesso ou limite  
3. **`Map.merge` + `BinaryOperator`** — contagem / agregação  
4. **Pipeline de domínio** — `Function` encadeada Entity → DTO → JSON-like  
5. **`BiConsumer` em eventos** — `(evento, handlerContext) → void`

---

## Armadilhas comuns

| Problema | Preferir |
|---|---|
| Lambda gigante no `stream` | Extrair método + method reference |
| `Consumer` com muita lógica de negócio | Use case / serviço nomeado |
| Boxing em loop quente | `IntStream` + FIs primitivas |
| Capturar estado mutável sem sync | `Atomic*` ou redesenhar |
| `forEach` com side effect complexo | `collect` / retorno explícito |

---

## Próximo

[Streams API](../streams/README.md) — onde essas interfaces viram pipeline.
