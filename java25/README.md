# java25 — Java 25 LTS: cobertura completa das JEPs

> Fonte oficial: [OpenJDK JDK 25](https://openjdk.org/projects/jdk/25/) · GA em **16/09/2025** · **18 JEPs**  
> Descrições e exemplos alinhados às JEPs oficiais. Módulo anterior: [`java21/`](../java21/README.md).

---

## Inventário das 18 JEPs

| # | JEP | Feature | Status | Demo |
|---|-----|---------|--------|------|
| 1 | [470](https://openjdk.org/jeps/470) | PEM Encodings of Cryptographic Objects | Preview | `PemEncodingDemo` |
| 2 | [502](https://openjdk.org/jeps/502) | Stable Values | Preview | `StableValuesDemo` |
| 3 | [503](https://openjdk.org/jeps/503) | Remove the 32-bit x86 Port | GA (impl.) | — |
| 4 | [505](https://openjdk.org/jeps/505) | Structured Concurrency | 5ª Preview | `StructuredConcurrencyDemo` |
| 5 | [506](https://openjdk.org/jeps/506) | Scoped Values | **GA** | `ScopedValuesDemo` |
| 6 | [507](https://openjdk.org/jeps/507) | Primitive Types in Patterns, instanceof, switch | 3ª Preview | `PrimitivePatternsDemo` |
| 7 | [508](https://openjdk.org/jeps/508) | Vector API | 10º Incubator | `VectorApiDemo` |
| 8 | [509](https://openjdk.org/jeps/509) | JFR CPU-Time Profiling | Experimental | CLI abaixo |
| 9 | [510](https://openjdk.org/jeps/510) | Key Derivation Function API | **GA** | `KdfApiDemo` |
| 10 | [511](https://openjdk.org/jeps/511) | Module Import Declarations | **GA** | `ModuleImportDemo` |
| 11 | [512](https://openjdk.org/jeps/512) | Compact Source Files and Instance Main Methods | **GA** | `CompactSourceDemo` |
| 12 | [513](https://openjdk.org/jeps/513) | Flexible Constructor Bodies | **GA** | `FlexibleConstructorsDemo` |
| 13 | [514](https://openjdk.org/jeps/514) | Ahead-of-Time Command-Line Ergonomics | **GA** | CLI abaixo |
| 14 | [515](https://openjdk.org/jeps/515) | Ahead-of-Time Method Profiling | **GA** | CLI abaixo |
| 15 | [518](https://openjdk.org/jeps/518) | JFR Cooperative Sampling | **GA** (impl.) | — |
| 16 | [519](https://openjdk.org/jeps/519) | Compact Object Headers | **GA** (produto) | CLI abaixo |
| 17 | [520](https://openjdk.org/jeps/520) | JFR Method Timing & Tracing | **GA** | CLI abaixo |
| 18 | [521](https://openjdk.org/jeps/521) | Generational Shenandoah | **GA** (produto) | CLI abaixo |

> Este módulo cobre o **release JDK 25** (as 18 JEPs acima). O delta LTS desde o JDK 21 inclui também JEPs das versões 22–24 — fora do escopo desta pasta.

---

## Índice

- [Linguagem (GA)](#linguagem-ga)
- [Bibliotecas (GA)](#bibliotecas-ga)
- [Runtime / HotSpot / JFR (GA e experimental)](#runtime--hotspot--jfr)
- [Preview](#preview)
- [Incubator](#incubator)
- [Como executar](#como-executar)
- [Referências](#referências)

---

# Linguagem (GA)

## JEP 511 — Module Import Declarations

📄 **Demo:** `src/br/com/murilo/imports/ModuleImportDemo.java`  
📎 [JEP 511](https://openjdk.org/jeps/511)

**Resumo oficial:** permite importar de forma sucinta todos os packages exportados por um módulo, simplificando o reuso de bibliotecas modulares — sem exigir que o código importador seja um módulo.

**Casos de uso**
- Prototipar / JShell / scripts sem dezenas de `import java.util.*`, `java.nio.file.*`, …
- Consumir APIs que cruzam módulos (ex.: `java.sql` + tipos de `java.xml` via dependência transitiva)
- Compact source files já se comportam como se tivessem `import module java.base;`

```java
import module java.base;
// List, Map, Stream, Path, Files, Collectors, Function…
```

Ambiguidade de simple names (`List` util vs AWT): resolva com `import java.util.List;`.

---

## JEP 512 — Compact Source Files and Instance Main Methods

📄 **Demo:** `src/CompactSourceDemo.java`  
📎 [JEP 512](https://openjdk.org/jeps/512)

**Resumo oficial:** facilita a escrita de programas pequenos — especialmente por iniciantes — permitindo *compact source files* (classe implícita) e métodos `main` de instância mais simples. Inclui a classe `IO` para entrada/saída básica.

**Casos de uso**
- Ensino / Hello World sem `public class` + `public static void main(String[] args)`
- Scripts one-file com `java Arquivo.java`
- Exploração rápida da API (`import module java.base` implícito)

```java
void main() {
    IO.println("Hello, Java 25!");
}
```

Em produção, prefira classes explícitas com `package`.

---

## JEP 513 — Flexible Constructor Bodies

📄 **Demo:** `src/br/com/murilo/constructors/FlexibleConstructorsDemo.java`  
📎 [JEP 513](https://openjdk.org/jeps/513)

**Resumo oficial:** no corpo de um construtor, permite statements **antes** de `super(...)` ou `this(...)`. Esse código não pode usar o objeto sob construção como receptor de métodos de instância, mas pode inicializar campos e fazer computações seguras — tornando construtores mais naturais e campos prontos antes de código da superclasse enxergar a subclasse.

**Casos de uso**
- Validar argumentos **antes** de chamar o construtor da superclasse (fail-fast)
- Preparar/computar argumentos do `super(...)`
- Inicializar campos da subclasse antes de métodos overridable chamados pelo `super`

```java
Employee(String name, int age) {
    if (age < 18 || age > 67)
        throw new IllegalArgumentException("idade inválida");
    super(name, age);
}
```

---

# Bibliotecas (GA)

## JEP 506 — Scoped Values

📄 **Demo:** `src/br/com/murilo/scoped/ScopedValuesDemo.java`  
📎 [JEP 506](https://openjdk.org/jeps/506)

**Resumo oficial:** scoped values permitem que um método compartilhe dados **imutáveis** com seus callees na mesma thread e com child threads. São mais fáceis de raciocinar que `ThreadLocal` e têm menor custo de espaço/tempo — especialmente com virtual threads e structured concurrency.

**Casos de uso**
- Contexto de request (user, tenant, correlation-id) sem poluir assinaturas
- Frameworks que precisam passar contexto interno através de código do usuário
- Substituir `ThreadLocal` quando o dado é imutável e de lifetime delimitado

```java
static final ScopedValue<String> USER = ScopedValue.newInstance();

ScopedValue.where(USER, "murilo").run(() -> handleRequest());
```

---

## JEP 510 — Key Derivation Function API

📄 **Demo:** `src/br/com/murilo/crypto/KdfApiDemo.java`  
📎 [JEP 510](https://openjdk.org/jeps/510)

**Resumo oficial:** API para Key Derivation Functions (KDFs) — algoritmos que derivam chaves adicionais a partir de uma chave secreta e outros dados. Finaliza o preview do JDK 24. Inclui implementação de HKDF; habilita uso em KEM/HPKE/TLS e providers PKCS#11.

**Casos de uso**
- Derivar chaves AES a partir de material compartilhado (HKDF-SHA256)
- Protocolos híbridos / pós-quânticos (bloco de construção de HPKE)
- Substituir HKDF “caseiro” ou APIs internas do JDK

```java
KDF hkdf = KDF.getInstance("HKDF-SHA256");
AlgorithmParameterSpec params = HKDFParameterSpec.ofExtract()
        .addIKM(initialKeyMaterial)
        .addSalt(salt)
        .thenExpand(info, 32);
SecretKey key = hkdf.deriveKey("AES", params);
```

> PBKDF1/PBKDF2 continuam via `SecretKeyFactory` (fora do escopo desta API).

---

# Runtime / HotSpot / JFR

## JEP 503 — Remove the 32-bit x86 Port

📎 [JEP 503](https://openjdk.org/jeps/503)

**Resumo oficial:** remove código-fonte e suporte de build do port **32-bit x86**, deprecado no JDK 24 (JEP 501). Desbloqueia features que exigiam fallbacks 32-bit e simplifica infra de build/teste.

**Casos de uso / impacto**
- Ambientes só 64-bit (padrão moderno) — sem mudança
- Legado em Linux/Windows 32-bit x86: migrar para JDK LTS anterior que ainda tenha o port, ou para 64-bit
- Não remove suporte 32-bit de **outras** arquiteturas (ex.: ARM32)

---

## JEP 514 — Ahead-of-Time Command-Line Ergonomics

📎 [JEP 514](https://openjdk.org/jeps/514)

**Resumo oficial:** facilita a criação de caches AOT (que aceleram o startup) simplificando os comandos dos casos comuns — sem perder expressividade do workflow em dois passos.

**Casos de uso**
- Training + create do cache AOT em **um** comando
- Apps cloud / microsserviços sensíveis a cold start
- Ferramentas (ex.: JRuby) que orquestram o próprio training

```bash
# Antes (2 passos): AOTMode=record depois AOTMode=create
# Agora (1 passo):
java -XX:AOTCacheOutput=app.aot -cp app.jar com.example.App ...

# Produção:
java -XX:AOTCache=app.aot -cp app.jar com.example.App ...
```

`JDK_AOT_VM_OPTIONS` permite opções só na sub-invocação de create. Em ambientes com pouca RAM, o workflow de 2 passos ainda pode ser preferível (o one-step dobra o heap momentaneamente).

---

## JEP 515 — Ahead-of-Time Method Profiling

📎 [JEP 515](https://openjdk.org/jeps/515)

**Resumo oficial:** melhora o warmup tornando perfis de execução de métodos de uma run anterior instantaneamente disponíveis no start da JVM, via AOT cache — o JIT pode gerar código nativo mais cedo, sem esperar coletar perfis do zero.

**Casos de uso**
- Reduzir tempo até peak performance após deploy
- Mesmo workflow AOT de 514/483 — perfis entram no cache automaticamente no training
- Sem alteração de código da aplicação

Produção continua perfilando online; perfis AOT aceleram o início do JIT, não o congelam.

---

## JEP 518 — JFR Cooperative Sampling

📎 [JEP 518](https://openjdk.org/jeps/518)

**Resumo oficial:** melhora a estabilidade do JFR ao amostrar stacks de forma assíncrona: o walk do call stack ocorre em **safepoints**, com mitigação de safepoint bias. Elimina heurísticas arriscadas que podiam crashar a JVM.

**Casos de uso / impacto**
- Profiling de execução mais confiável em produção
- Base para JEP 509 (CPU-time profiling)
- Transparente para a aplicação — benefício interno do JFR

---

## JEP 519 — Compact Object Headers

📎 [JEP 519](https://openjdk.org/jeps/519)

**Resumo oficial:** promove compact object headers de experimental (JDK 24) a **product feature**. Reduz o tamanho do object header, melhorando densidade de deploy e localidade; em benchmarks oficiais, menos heap/CPU e menos GCs.

**Casos de uso**
- Heaps grandes com muitos objetos pequenos
- Serviços com pressão de memória / GC frequente

```bash
# Não precisa mais de UnlockExperimentalVMOptions
java -XX:+UseCompactObjectHeaders -jar app.jar
```

Ainda **não** é o layout default (ver evolução em JEPs posteriores).

---

## JEP 520 — JFR Method Timing & Tracing

📎 [JEP 520](https://openjdk.org/jeps/520)

**Resumo oficial:** estende o JFR com timing e tracing de métodos via instrumentação de bytecode — estatísticas exatas (não só amostragem), sem mudar o código-fonte. Filtros por método, classe ou annotation; CLI, `.jfc`, `jcmd`, JMX.

**Casos de uso**
- Descobrir o que dispara `HashMap.resize`
- Medir `<clinit>` lentos no startup
- Contar/medir endpoints anotados (`@GET`, annotations custom)
- Caçar vazamento de `FileDescriptor` (trace `<init>` vs `close`)

```bash
java -XX:StartFlightRecording:jdk.MethodTrace#filter=java.util.HashMap::resize,filename=rec.jfr ...
jfr print --events jdk.MethodTrace --stack-depth 20 rec.jfr

java '-XX:StartFlightRecording:method-timing=::<clinit>,filename=clinit.jfr' ...
jfr view method-timing clinit.jfr
```

---

## JEP 509 — JFR CPU-Time Profiling (Experimental)

📎 [JEP 509](https://openjdk.org/jeps/509)

**Resumo oficial:** aprimora o JFR para capturar profiling de **CPU-time** mais preciso no **Linux**, usando o timer de CPU do kernel (não só wall-clock). Experimental. Evento `jdk.CPUTimeSample`.

**Casos de uso**
- Distinguir métodos CPU-bound vs I/O-bound com latência parecida
- Continuous profiling em produção com `throttle` baixo
- Flame graphs / `jfr view cpu-time-hot-methods`

```bash
java -XX:StartFlightRecording=jdk.CPUTimeSample#enabled=true,filename=profile.jfr ...
jfr view cpu-time-hot-methods profile.jfr
```

---

## JEP 521 — Generational Shenandoah

📎 [JEP 521](https://openjdk.org/jeps/521)

**Resumo oficial:** promove o modo generational do Shenandoah de experimental a **product feature**. Default do Shenandoah continua single-generation; o modo generational não exige mais `UnlockExperimentalVMOptions`.

**Casos de uso**
- Workloads com muitos objetos young + requisito de baixa latência
- Alternativa a G1/ZGC quando Shenandoah já é a escolha

```bash
java -XX:+UseShenandoahGC -XX:ShenandoahGCMode=generational -jar app.jar
```

---

# Preview

> Compile/execute com `--enable-preview`. APIs podem mudar antes do GA.

## JEP 470 — PEM Encodings of Cryptographic Objects (Preview)

📄 **Demo:** `src/br/com/murilo/crypto/PemEncodingDemo.java`  
📎 [JEP 470](https://openjdk.org/jeps/470)

**Resumo oficial:** API para encode/decode de chaves, certificados e CRLs no formato PEM (RFC 7468) — o mesmo texto `BEGIN … / END …` usado por OpenSSL, CAs, OpenSSH, YubiKeys.

**Casos de uso**
- Ler/gravar chaves PEM sem parsing manual + Base64
- Interoperar com OpenSSL / certificados emitidos por CA
- Criptografar private keys PEM com password (`withEncryption`)

```java
String pem = PEMEncoder.of().encodeToString(publicKey);
PublicKey key = PEMDecoder.of().decode(pem, PublicKey.class);
```

---

## JEP 502 — Stable Values (Preview)

📄 **Demo:** `src/br/com/murilo/stable/StableValuesDemo.java`  
📎 [JEP 502](https://openjdk.org/jeps/502)

**Resumo oficial:** objetos que guardam dados imutáveis, tratados como constantes pela JVM (constant-folding como `final`), porém com inicialização **sob demanda** e no máximo uma vez — inclusive sob concorrência.

**Casos de uso**
- Logger / cliente HTTP / pool inicializado na primeira uso (sem double-checked locking)
- Quebrar inicialização monolítica no startup da aplicação
- `StableValue.supplier` / `StableValue.list` para composição

```java
private final StableValue<Logger> logger = StableValue.of();

Logger getLogger() {
    return logger.orElseSet(() -> Logger.create(MyClass.class));
}
```

---

## JEP 505 — Structured Concurrency (Fifth Preview)

📄 **Demo:** `src/br/com/murilo/concurrency/StructuredConcurrencyDemo.java`  
📎 [JEP 505](https://openjdk.org/jeps/505)

**Resumo oficial:** API que trata grupos de tarefas relacionadas em threads distintas como **uma unidade de trabalho** — melhor error handling, cancelamento, confiabilidade e observabilidade. No JDK 25, `StructuredTaskScope` abre via factories estáticas (`open()`), com `Joiner` para políticas.

**Casos de uso**
- Agregar `findUser()` + `fetchOrder()` sem thread leak
- Cancelar irmãos quando uma subtarefa falha
- Fan-out I/O com virtual threads
- Políticas: all-success, any-success, custom `Joiner`

```java
try (var scope = StructuredTaskScope.open()) {
    var user  = scope.fork(() -> findUser());
    var order = scope.fork(() -> fetchOrder());
    scope.join();
    return new Response(user.get(), order.get());
}
```

---

## JEP 507 — Primitive Types in Patterns, instanceof, and switch (Third Preview)

📄 **Demo:** `src/br/com/murilo/patterns/PrimitivePatternsDemo.java`  
📎 [JEP 507](https://openjdk.org/jeps/507)

**Resumo oficial:** permite tipos primitivos em todos os contextos de pattern matching e estende `instanceof` / `switch` para trabalhar com todos os primitivos — exploração uniforme de dados e casts seguros (sem perda silenciosa de informação).

**Casos de uso**
- `switch` em `int`/`long`/`double` com patterns e guards
- `instanceof int i` / pattern em record components primitivos
- Substituir `default` genérico por `case int i -> …`

```java
String label = switch (status) {
    case 0 -> "okay";
    case 1 -> "warning";
    case 2 -> "error";
    case int i -> "unknown: " + i;
};
```

---

# Incubator

## JEP 508 — Vector API (Tenth Incubator)

📄 **Demo:** `src/br/com/murilo/vector/VectorApiDemo.java`  
📎 [JEP 508](https://openjdk.org/jeps/508)

**Resumo oficial:** API para expressar computações vetoriais que a JVM compila de forma confiável para instruções SIMD do CPU, com performance superior ao equivalente escalar. Continua em incubator até Valhalla; no 25, `VectorShuffle` ↔ `MemorySegment`, math via FFM, auto-vectorização Float16 em x64 suportado.

**Casos de uso**
- Processamento numérico / ML / codecs / image
- Hot loops de float/int onde SIMD importa

```java
// requer --add-modules jdk.incubator.vector
var species = FloatVector.SPECIES_PREFERRED;
FloatVector a = FloatVector.fromArray(species, left, 0);
FloatVector b = FloatVector.fromArray(species, right, 0);
a.add(b).intoArray(result, 0);
```

---

## Como executar

Requer **JDK 25+**.

```bash
cd java25/src

# ── GA ──────────────────────────────────────────────
javac br/com/murilo/scoped/ScopedValuesDemo.java
java -cp . br.com.murilo.scoped.ScopedValuesDemo

javac br/com/murilo/constructors/FlexibleConstructorsDemo.java
java -cp . br.com.murilo.constructors.FlexibleConstructorsDemo

javac br/com/murilo/imports/ModuleImportDemo.java
java -cp . br.com.murilo.imports.ModuleImportDemo

java CompactSourceDemo.java

javac br/com/murilo/crypto/KdfApiDemo.java
java -cp . br.com.murilo.crypto.KdfApiDemo

# ── Preview ─────────────────────────────────────────
javac --enable-preview --release 25 br/com/murilo/stable/StableValuesDemo.java
java --enable-preview -cp . br.com.murilo.stable.StableValuesDemo

javac --enable-preview --release 25 br/com/murilo/concurrency/StructuredConcurrencyDemo.java
java --enable-preview -cp . br.com.murilo.concurrency.StructuredConcurrencyDemo

javac --enable-preview --release 25 br/com/murilo/patterns/PrimitivePatternsDemo.java
java --enable-preview -cp . br.com.murilo.patterns.PrimitivePatternsDemo

javac --enable-preview --release 25 br/com/murilo/crypto/PemEncodingDemo.java
java --enable-preview -cp . br.com.murilo.crypto.PemEncodingDemo

# ── Incubator ───────────────────────────────────────
javac --add-modules jdk.incubator.vector br/com/murilo/vector/VectorApiDemo.java
java --add-modules jdk.incubator.vector -cp . br.com.murilo.vector.VectorApiDemo
```

---

## Referências

- [OpenJDK JDK 25](https://openjdk.org/projects/jdk/25/)
- JEPs: [470](https://openjdk.org/jeps/470) · [502](https://openjdk.org/jeps/502) · [503](https://openjdk.org/jeps/503) · [505](https://openjdk.org/jeps/505) · [506](https://openjdk.org/jeps/506) · [507](https://openjdk.org/jeps/507) · [508](https://openjdk.org/jeps/508) · [509](https://openjdk.org/jeps/509) · [510](https://openjdk.org/jeps/510) · [511](https://openjdk.org/jeps/511) · [512](https://openjdk.org/jeps/512) · [513](https://openjdk.org/jeps/513) · [514](https://openjdk.org/jeps/514) · [515](https://openjdk.org/jeps/515) · [518](https://openjdk.org/jeps/518) · [519](https://openjdk.org/jeps/519) · [520](https://openjdk.org/jeps/520) · [521](https://openjdk.org/jeps/521)
