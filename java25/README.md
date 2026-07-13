# java25 — Java 25 LTS: Novos Recursos Essenciais

> **Java 25** é o LTS sucessor do Java 21 (GA em 16/09/2025).  
> Este módulo cobre features **finalizadas (GA)** mais relevantes para estudo e uso diário,  
> e resume melhorias de runtime / previews.

---

## Índice

- [Scoped Values (JEP 506)](#1-scoped-values--jep-506-ga)
- [Flexible Constructor Bodies (JEP 513)](#2-flexible-constructor-bodies--jep-513-ga)
- [Compact Source Files & Instance Main (JEP 512)](#3-compact-source-files--instance-main--jep-512-ga)
- [Module Import Declarations (JEP 511)](#4-module-import-declarations--jep-511-ga)
- [Melhorias de runtime](#5-melhorias-de-runtime)
- [Ainda preview / incubator](#ainda-preview--incubator)
- [Delta desde o Java 21](#delta-desde-o-java-21)
- [Como executar](#como-executar)
- [Referências](#referências)

---

## 1. Scoped Values — JEP 506 (GA)

📄 **Demo:** `src/br/com/murilo/scoped/ScopedValuesDemo.java`

### O problema — ThreadLocal

`ThreadLocal` compartilha dados implícitos na thread, mas:
- lifetime **ilimitado** (fácil vazar memória se esquecer `remove()`)
- mutável → difícil de raciocinar
- herança para child threads é cara / frágil

### A solução — Scoped Values

Valor **imutável**, ligado a um escopo sintático (`where(...).run/call`), visível para callees e para child threads (ex.: virtual threads + structured concurrency).

```java
static final ScopedValue<String> USER = ScopedValue.newInstance();

ScopedValue.where(USER, "murilo").run(() -> {
    // qualquer método chamado daqui vê USER.get() == "murilo"
    handleRequest();
});
// binding destruído ao sair do run
```

### Quando usar

| Cenário | Scoped Value | ThreadLocal |
|---|---|---|
| Contexto de request (user, tenant, correlation-id) | ✅ | ⚠️ |
| Framework que chama código do usuário e precisa de contexto interno | ✅ | ⚠️ |
| Estado mutável por thread | ❌ | ✅ (com cuidado) |
| Dados que precisam sobreviver após o escopo | ❌ | ❌ (repensar design) |

> **Par natural de Virtual Threads (Java 21):** milhares de VTs + contexto imutável sem o custo de ThreadLocal.

---

## 2. Flexible Constructor Bodies — JEP 513 (GA)

📄 **Demo:** `src/br/com/murilo/constructors/FlexibleConstructorsDemo.java`

Antes, a **primeira** statement de um construtor tinha que ser `super(...)` ou `this(...)`.  
Agora você pode executar código **antes** — desde que não use `this` como objeto pronto (pode atribuir campos).

```java
class Employee extends Person {
    Employee(String name, int age) {
        // ✅ validação / preparação ANTES do super
        if (age < 18 || age > 67)
            throw new IllegalArgumentException("idade inválida: " + age);
        super(name, age);
    }
}
```

### Benefícios

1. **Fail-fast** — rejeita args antes de construir a superclasse  
2. **Integridade** — pode inicializar campos da subclasse antes do `super` chamar métodos overridable  
3. Menos métodos auxiliares estáticos só para “passar validação no super(...)”

---

## 3. Compact Source Files & Instance Main — JEP 512 (GA)

📄 **Demo:** `src/CompactSourceDemo.java` (sem `package` / sem `public class` explícito obrigatório)

Reduz o “ritual” do Hello World para aprendizado e scripts:

```java
void main() {
    IO.println("Hello, Java 25!");
}
```

- Classe implícita (compact source file)
- `main` de instância sem `String[] args` (ou com)
- Em compact sources, é como se houvesse `import module java.base;`
- `IO` (java.lang) para println/readln simples

> Use para prototipar / ensinar. Em código de produção, prefira classes explícitas com package.

---

## 4. Module Import Declarations — JEP 511 (GA)

📄 **Demo:** `src/br/com/murilo/imports/ModuleImportDemo.java`

Importa **todos** os packages exportados por um módulo de uma vez:

```java
import module java.base;

// List, Map, Stream, Path, Files… disponíveis pelo simple name
```

Equivalente a dezenas de `import java.util.*`, `import java.nio.file.*`, etc.

### Ambiguidade

Se dois módulos exportam o mesmo simple name (`List` de `java.util` vs `java.awt`), resolva com import específico:

```java
import module java.base;
import module java.desktop;
import java.util.List;   // desambigua
```

---

## 5. Melhorias de runtime

Sem demo de código — impacto em produção / ops:

| JEP | Recurso | Efeito prático |
|---|---|---|
| **519** | Compact Object Headers (GA) | Headers menores → menos heap (~10–20% em workloads típicos), melhor localidade, menos pressão no GC |
| **521** | Generational Shenandoah (GA) | Shenandoah com gerações → latência baixa + throughput melhor em heaps grandes |
| **514 / 515** | AOT ergonomics + method profiling | Criar/usar AOT cache mais simples; profiling alimenta warm-up mais rápido |
| **510** | Key Derivation Function API | API padrão para derivar chaves (crypto) |
| **503** | Remove 32-bit x86 port | Só 64-bit x86 (e outros arches suportados) |

JFR também ganhou sampling cooperativo, method timing/tracing e CPU-time profiling experimental (JEPs 518, 520, 509).

---

## Ainda preview / incubator

Não são GA — acompanhar, não basear APIs estáveis neles:

| JEP | Recurso | Status na 25 |
|---|---|---|
| 505 | Structured Concurrency | 5ª preview |
| 507 | Primitive Types in Patterns / instanceof / switch | 3ª preview |
| 502 | Stable Values | Preview |
| 470 | PEM Encodings of Cryptographic Objects | Preview |
| 508 | Vector API | 10º incubator |

---

## Delta desde o Java 21

O que o roadmap antigo do `java21/` esperava vs o que de fato chegou:

| Recurso | Expectativa antiga | Realidade na JDK 25 |
|---|---|---|
| Scoped Values | “GA no 23” | **GA no 25** (JEP 506) |
| Structured Concurrency | “GA no 23” | Ainda **preview** (JEP 505) |
| Compact Source / instance main | Preview 21 | **GA** (JEP 512) |
| Flexible constructors | — | **GA** (JEP 513) |
| Module import | — | **GA** (JEP 511) |
| Value Objects (Valhalla) | “esperado no 25” | **Não chegou** |

Módulo anterior: [`java21/`](../java21/README.md).

---

## Como executar

Requer **JDK 25+**.

```bash
cd java25/src

# Scoped Values
javac br/com/murilo/scoped/ScopedValuesDemo.java
java -cp . br.com.murilo.scoped.ScopedValuesDemo

# Flexible constructors
javac br/com/murilo/constructors/FlexibleConstructorsDemo.java
java -cp . br.com.murilo.constructors.FlexibleConstructorsDemo

# Module import
javac br/com/murilo/imports/ModuleImportDemo.java
java -cp . br.com.murilo.imports.ModuleImportDemo

# Compact source (source-file mode)
java CompactSourceDemo.java
```

---

## Referências

- [OpenJDK JDK 25](https://openjdk.org/projects/jdk/25/)
- [JEP 506 — Scoped Values](https://openjdk.org/jeps/506)
- [JEP 513 — Flexible Constructor Bodies](https://openjdk.org/jeps/513)
- [JEP 512 — Compact Source Files and Instance Main Methods](https://openjdk.org/jeps/512)
- [JEP 511 — Module Import Declarations](https://openjdk.org/jeps/511)
- [JEP 519 — Compact Object Headers](https://openjdk.org/jeps/519)
- [JEP 521 — Generational Shenandoah](https://openjdk.org/jeps/521)
