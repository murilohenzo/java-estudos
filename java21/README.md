# mod_java21 — Java 21: Novos Recursos Essenciais

> **Java LTS 21** foi o LTS anterior ao Java 25.  
> Este módulo cobre os recursos essenciais da 21. Para o LTS atual, veja [`java25/`](../java25/README.md).

---

## Índice

- [Virtual Threads (JEP 444)](#1-virtual-threads--jep-444-ga)
- [Pattern Matching for switch (JEP 441)](#2-pattern-matching-for-switch--jep-441-ga)
- [Record Patterns (JEP 440)](#3-record-patterns--jep-440-ga)
- [Sequenced Collections (JEP 431)](#4-sequenced-collections--jep-431-ga)
- [Unnamed Variables & Patterns (JEP 443)](#5-unnamed-variables--patterns--jep-443-preview)
- [Sealed Classes — revisão completa](#6-sealed-classes--revisão-completa-jep-409-ga-java-17)
- [Recursos vistos em outros módulos](#recursos-já-cobertos-em-outros-módulos)
- [Próximo LTS — Java 25](#próximo-lts--java-25)

---

## 1. Virtual Threads — JEP 444 (GA)

📄 **Demo:** `src/br/com/murilo/virtualthreads/VirtualThreadsDemo.java`

### O problema — Thread Starvation

No modelo clássico (thread de plataforma = thread do SO):
- Cada OS thread consome **~1 MB** de stack + overhead do kernel
- Um servidor com pool de 200 threads fica **bloqueado** esperando I/O
- Para escalar: reactive programming (código complexo, debugging difícil)

### A solução — Virtual Threads

```
Virtual Thread 1 ─┐
Virtual Thread 2 ─┤ → montadas em → Platform Thread (carrier)
Virtual Thread N ─┘                 (pool pequeno de OS threads)
```

- Uma virtual thread consome apenas **~few KB**
- Ao bloquear em I/O, é **desmontada** do carrier — que fica livre para outras
- Permite **milhões** de threads simultâneas

### API

```java
// Thread.ofVirtual() — forma explícita
Thread vt = Thread.ofVirtual().name("worker-1").start(() -> {
    // código aqui — pode bloquear em I/O à vontade
    doWork();
});

// Thread.startVirtualThread() — atalho conveniente
Thread.startVirtualThread(this::processRequest);

// ExecutorService — padrão para servidores
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (var request : requests) {
        executor.submit(() -> handleRequest(request));  // 1 virtual thread por request
    }
}  // aguarda todas ao fechar
```

### Diagnóstico: isVirtual()

```java
Thread.currentThread().isVirtual();  // true se for virtual thread
```

### Quando usar vs não usar

| Cenário | Virtual Threads | Pool clássico |
|---|---|---|
| Servidor HTTP (1000s req/s) | ✅ ideal | ❌ limitado |
| Chamadas JDBC / I/O | ✅ ideal | ❌ bloqueante |
| Cálculo CPU intensivo | ❌ não melhora | ✅ ForkJoinPool |
| Locks sincronizados lontos | ⚠️ pinning issue | ✅ ok |

> **Pinning:** Virtual threads ainda são "pinadas" ao carrier durante `synchronized` blocks.  
> Prefira `ReentrantLock` em código de alta concorrência.

---

## 2. Pattern Matching for switch — JEP 441 (GA)

📄 **Demo:** `src/br/com/murilo/patterns/PatternMatchingSwitchDemo.java`

### Antes vs Depois

```java
// ❌ Java 16 — cascata verbosa
if      (shape instanceof Circle c)    { return Math.PI * c.radius() * c.radius(); }
else if (shape instanceof Rectangle r) { return r.width() * r.height(); }
else throw new IllegalStateException();

// ✅ Java 21 — switch com type patterns
double area = switch (shape) {
    case Circle c     -> Math.PI * c.radius() * c.radius();
    case Rectangle r  -> r.width() * r.height();
    case Triangle t   -> 0.5 * t.base() * t.height();
    // Sem default — sealed garante exaustividade em compile time
};
```

### Guarded Patterns — cláusula `when`

```java
String category = switch (shape) {
    case Circle c when c.radius() > 100 -> "grande";
    case Circle c when c.radius() > 10  -> "médio";
    case Circle c                       -> "pequeno";
    default -> "outro";
};
```

### null agora é tratado explicitamente

```java
// Antes: lançava NullPointerException
// Agora:
String result = switch (obj) {
    case null      -> "nulo";
    case Integer i -> "int: " + i;
    case String s  -> "str: " + s;
    default        -> "outro";
};
```

---

## 3. Record Patterns — JEP 440 (GA)

📄 **Demo:** `src/br/com/murilo/patterns/PatternMatchingSwitchDemo.java`

Desestrutura records diretamente — acessa componentes sem getters explícitos:

```java
record Point(int x, int y) {}
record Circle(Point center, double radius) {}

// Record pattern em switch
String describe = switch (shape) {
    case Circle(Point(var x, var y), var r) when r > 0
        -> "círculo em (%d,%d) r=%.1f".formatted(x, y, r);
    case Circle(var _, var r) when r <= 0
        -> "círculo inválido";
    default -> "outra forma";
};
```

### Com instanceof

```java
// Desestruturação em linha única
if (event instanceof Purchase(var user, var amount) && amount > 1000) {
    notifyAudit(user, amount);
}
```

---

## 4. Sequenced Collections — JEP 431 (GA)

📄 **Demo:** `src/br/com/murilo/collections/SequencedCollectionsDemo.java`

### Nova hierarquia

```
Collection
  └── SequencedCollection<E>    ← nova
        ├── List<E>
        └── SequencedSet<E>     ← nova
              ├── SortedSet<E>
              └── LinkedHashSet

Map
  └── SequencedMap<K,V>         ← nova
        ├── SortedMap<K,V>
        └── LinkedHashMap
```

### API unificada

```java
SequencedCollection<String> sc = new ArrayList<>(List.of("a", "b", "c"));

sc.getFirst();    // "a"
sc.getLast();     // "c"
sc.addFirst("z"); // inserção eficiente no início
sc.addLast("z");  // inserção eficiente no fim
sc.removeFirst(); // remoção do início
sc.removeLast();  // remoção do fim
sc.reversed();    // visão invertida (lazy, sem cópia)
```

### SequencedMap

```java
SequencedMap<String, Integer> sm = new LinkedHashMap<>();
sm.firstEntry();        // Map.Entry do primeiro
sm.lastEntry();         // Map.Entry do último
sm.putFirst("a", 1);    // inserção no início
sm.pollFirstEntry();    // remove e retorna o primeiro
sm.reversed();          // visão reversa do mapa
```

---

## 5. Unnamed Variables & Patterns — JEP 443 (preview)

📄 **Demo:** `src/br/com/murilo/misc/UnnamedVariablesDemo.java`

O caractere `_` (underscore) declara uma variável **intencionalmente ignorada**:

```java
// Switch — ignora bindings desnecessários
switch (event) {
    case Login(var user, var _)  -> audit(user);   // IP ignorado
    case Purchase(var _, var amt) -> total += amt; // user ignorado
    case PageView _               -> {}            // tipo inteiro ignorado
}

// catch — exceção não usada
try {
    return Integer.parseInt(s);
} catch (NumberFormatException _) {
    return fallback;
}

// for-each — elemento não usado (apenas efeito colateral)
for (var _ : tasks) {
    counter.incrementAndGet();
}
```

**Benefício:** elimina nomes `dummy`, `unused`, `ignored` — `_` é inequívoco.

---

## 6. Sealed Classes — revisão completa (JEP 409 GA, Java 17)

Sealed classes definem um conjunto **fechado** de subtipos — o compilador conhece todas as implementações possíveis.

```java
// Hierarquia sealed completa
sealed interface Result<T> permits Result.Ok, Result.Err {
    record Ok<T>(T value)        implements Result<T> {}
    record Err<T>(String message) implements Result<T> {}
}

// Uso — switch exaustivo sem default
<T> String render(Result<T> result) {
    return switch (result) {
        case Result.Ok<T>  ok  -> "✓ " + ok.value();
        case Result.Err<T> err -> "✗ " + err.message();
        // Compilador verifica exaustividade — se adicionar novo subtipo,
        // todos os switches que não têm default falham em compile time
    };
}
```

### Regras das Sealed Classes

```java
sealed class Shape permits Circle, Rectangle, Triangle {}

// Cada subtipo deve ser: final, sealed, ou non-sealed
final class Circle    extends Shape {}  // sem mais subclasses
sealed class Polygon  extends Shape permits Triangle, Square {}  // pode ter filhos
non-sealed class Blob extends Shape {}  // sem restrições (abre a hierarquia)
```

---

## Recursos já cobertos em outros módulos

| Recurso | JEP | Módulo |
|---|---|---|
| Records | JEP 395 | `mod31` (CourseRecord) |
| Text Blocks | JEP 378 | `mod31` (SQL em CourseDao) |
| Pattern matching `instanceof` | JEP 394 | `mod31` (equals), `mod13` |
| switch expressions (arrow) | JEP 361 | `mod18`, `mod31` (Status) |
| `var` (local type inference) | JEP 286 | Usado em toda a base |
| `Stream.toList()` | JDK 16 | `mod22` |
| `String.formatted()` | JDK 15 | `mod31` (toString) |

---

## Próximo LTS — Java 25

O LTS seguinte é o **Java 25** (GA 16/09/2025). Status real vs o que se especulava na era 21:

| Recurso | Status na JDK 25 |
|---|---|
| Scoped Values | **GA** (JEP 506) |
| Flexible Constructor Bodies | **GA** (JEP 513) |
| Compact Source Files & Instance Main | **GA** (JEP 512) |
| Module Import Declarations | **GA** (JEP 511) |
| Structured Concurrency | Ainda preview (JEP 505) |
| Value Objects (Valhalla) | **Não chegou** |

Documento completo + demos: [`../java25/README.md`](../java25/README.md).

---

## Como executar

```bash
cd mod_java21
# Java 21+ obrigatório
javac --enable-preview --release 21 src/**/*.java
java  --enable-preview -cp src br.com.murilo.virtualthreads.VirtualThreadsDemo
java  --enable-preview -cp src br.com.murilo.patterns.PatternMatchingSwitchDemo
java  --enable-preview -cp src br.com.murilo.collections.SequencedCollectionsDemo
java  --enable-preview -cp src br.com.murilo.misc.UnnamedVariablesDemo
```

---

## Referências

- [JEP 444 — Virtual Threads](https://openjdk.org/jeps/444)
- [JEP 441 — Pattern Matching for switch](https://openjdk.org/jeps/441)
- [JEP 440 — Record Patterns](https://openjdk.org/jeps/440)
- [JEP 431 — Sequenced Collections](https://openjdk.org/jeps/431)
- [JEP 443 — Unnamed Variables & Patterns](https://openjdk.org/jeps/443)
- [JEP 409 — Sealed Classes](https://openjdk.org/jeps/409)
