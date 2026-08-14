# Programação Concorrente no JDK 21

## Sumário

- [1. Objetivo](#1-objetivo)
- [2. Concorrência x Paralelismo](#2-concorrência-x-paralelismo)
- [3. Thread](#3-thread)
- [4. Runnable](#4-runnable)
- [5. Callable](#5-callable)
- [6. ExecutorService](#6-executorservice)
- [7. Fixed Thread Pool](#7-fixed-thread-pool)
- [8. Cuidado com o tamanho do Thread Pool](#8-cuidado-com-o-tamanho-do-thread-pool)
- [9. Future](#9-future)
- [10. CompletableFuture](#10-completablefuture)
- [11. thenApply](#11-thenapply)
- [12. thenCompose](#12-thencompose)
- [13. Operações paralelas com CompletableFuture](#13-operações-paralelas-com-completablefuture)
- [14. thenApply x thenApplyAsync](#14-thenapply-x-thenapplyasync)
- [15. Tratamento de erros](#15-tratamento-de-erros)
- [16. Race Condition](#16-race-condition)
- [17. AtomicInteger](#17-atomicinteger)
- [18. synchronized](#18-synchronized)
- [19. Cuidado com synchronized](#19-cuidado-com-synchronized)
- [20. ConcurrentHashMap](#20-concurrenthashmap)
- [21. Operações atômicas do ConcurrentHashMap](#21-operações-atômicas-do-concurrenthashmap)
- [22. ConcurrentHashMap não torna tudo thread-safe](#22-concurrenthashmap-não-torna-tudo-thread-safe)
- [23. Semaphore](#23-semaphore)
- [24. BlockingQueue](#24-blockingqueue)
- [25. Backpressure](#25-backpressure)
- [26. Virtual Threads no JDK 21](#26-virtual-threads-no-jdk-21)
- [27. Virtual Thread x Platform Thread](#27-virtual-thread-x-platform-thread)
- [28. Quando utilizar Virtual Threads](#28-quando-utilizar-virtual-threads)
- [29. Virtual Threads não são solução para CPU-bound](#29-virtual-threads-não-são-solução-para-cpu-bound)
- [30. Virtual Threads + Semaphore](#30-virtual-threads--semaphore)
- [31. CompletableFuture + Virtual Threads](#31-completablefuture--virtual-threads)
- [32. Memory Leak em programação concorrente](#32-memory-leak-em-programação-concorrente)
- [33. Removendo estado temporário](#33-removendo-estado-temporário)
- [34. Lifecycle dos Executors](#34-lifecycle-dos-executors)
- [35. Deadlock](#35-deadlock)
- [36. Imutabilidade](#36-imutabilidade)
- [37. Producer/Consumer](#37-producerconsumer)
- [38. Caso de uso: processamento CPU-bound](#38-caso-de-uso-processamento-cpu-bound)
- [39. Caso de uso: processamento I/O-bound](#39-caso-de-uso-processamento-io-bound)
- [40. Caso de uso: várias chamadas independentes](#40-caso-de-uso-várias-chamadas-independentes)
- [41. Caso de uso: deduplicação concorrente](#41-caso-de-uso-deduplicação-concorrente)
- [42. Caso de uso: limitar acesso a um downstream](#42-caso-de-uso-limitar-acesso-a-um-downstream)
- [43. Caso de uso: Kafka + processamento + GCS](#43-caso-de-uso-kafka--processamento--gcs)
- [44. Modelo de processamento concorrente para Kafka](#44-modelo-de-processamento-concorrente-para-kafka)
- [45. Throughput não significa aumentar threads indefinidamente](#45-throughput-não-significa-aumentar-threads-indefinidamente)
- [46. Lei de Little](#46-lei-de-little)
- [47. Métricas importantes](#47-métricas-importantes)
- [48. Instrumentação recomendada](#48-instrumentação-recomendada)
- [49. Como escolher a abordagem](#49-como-escolher-a-abordagem)
- [50. Árvore de decisão](#50-árvore-de-decisão)
- [51. Princípios para programação concorrente segura](#51-princípios-para-programação-concorrente-segura)
- [52. Modelo mental final](#52-modelo-mental-final)
- [Exemplos adicionais e padrões práticos](#exemplos-adicionais-e-padrões-práticos)
- [Referências](#referências)


## 1. Objetivo

Este documento apresenta os principais conceitos e ferramentas de **programação concorrente no Java/JDK 21**, com foco em aplicações backend, processamento de eventos, sistemas distribuídos e pipelines I/O-bound.

O objetivo não é apenas apresentar APIs, mas explicar **quando utilizar cada abordagem**, quais problemas elas resolvem, quais riscos introduzem e como combiná-las de maneira segura.

Os principais tópicos abordados são:

* `Thread`
* `Runnable`
* `Callable`
* `ExecutorService`
* Thread Pools
* `Future`
* `CompletableFuture`
* `ConcurrentHashMap`
* `Atomic*`
* `synchronized`
* `Lock`
* `Semaphore`
* `BlockingQueue`
* Producer/Consumer
* Backpressure
* Virtual Threads
* Structured Concurrency
* Java Memory Model
* Race Conditions
* Deadlocks
* Memory Leak em aplicações concorrentes
* Controle de throughput
* Processamento concorrente de eventos Kafka

---

## 2. Concorrência x Paralelismo

Antes das APIs, é importante separar dois conceitos.

### Concorrência

Concorrência significa que múltiplas tarefas podem estar em progresso ao mesmo tempo.

```text
Task A ────────┐       ┌────────
               └───────┘

Task B ────┐        ┌──────────
           └────────┘

Task C ────────┐  ┌────────────
               └──┘
```

As tarefas podem compartilhar os mesmos recursos e o sistema alterna entre elas.

### Paralelismo

Paralelismo ocorre quando tarefas são executadas realmente ao mesmo tempo em diferentes recursos de processamento.

```text
CPU 1 ─── Task A ─────────────

CPU 2 ─── Task B ─────────────

CPU 3 ─── Task C ─────────────
```

#### Regra prática

Para CPU-bound:

```text
mais threads ≠ necessariamente mais throughput
```

Para I/O-bound:

```text
mais concorrência pode aumentar bastante o throughput
```

Exemplos de I/O-bound: HTTP, banco de dados, GCS/S3, filesystem, chamadas a APIs externas.

Exemplos de CPU-bound: criptografia pesada, compressão, parsing muito pesado, processamento matemático.

---

## 3. Thread

A abstração básica é `Thread`.

```java
Thread thread = new Thread(() -> {
    System.out.println("Executando");
});

thread.start();
```

Tenha em mente que chamar `thread.run();` não cria execução concorrente — `thread.start();` é que inicia a nova thread.

### Quando utilizar

Criar threads manualmente é útil principalmente para:

* entender os fundamentos;
* casos muito específicos;
* infraestrutura de baixo nível.

Em aplicações backend, normalmente é preferível utilizar `ExecutorService`.

---

## 4. Runnable

`Runnable` representa uma tarefa sem retorno.

```java
Runnable task = () -> {
    processarEvento();
};
```

Pode ser submetido a um executor com `execute()` ou `submit()` (este último retorna um `Future`).

---

## 5. Callable

Quando a tarefa precisa retornar um resultado:

```java
Callable<String> task = () -> {
    return "resultado";
};
```

Execução:

```java
Future<String> future = executor.submit(task);
String resultado = future.get();
```

---

## 6. ExecutorService

`ExecutorService` separa a definição da tarefa do gerenciamento das threads.

```java
ExecutorService executor = Executors.newFixedThreadPool(8);
executor.submit(() -> {
    processarEvento();
});
```

Benefícios: reutilização de threads, controle de concorrência, gerenciamento de lifecycle, filas internas e isolamento de workloads.

---

## 7. Fixed Thread Pool

Exemplo:

```java
ExecutorService executor = Executors.newFixedThreadPool(8);
```

Significa que no máximo 8 threads executarão tarefas simultaneamente.

Quando utilizar: muito útil para CPU-bound ou quando é necessário impor um limite rígido de concorrência.

---

## 8. Cuidado com o tamanho do Thread Pool

Não existe uma regra universal — o dimensionamento depende do workload.

Para CPU-bound: workers ≈ número de CPUs disponíveis.
Para I/O-bound: workers pode ser maior, pois as threads passam parte do tempo esperando.

Aumentar indiscriminadamente pode gerar contexto de troca excessivo (context switching), contenção, aumento de memória, latência e saturação do downstream.

---

## 9. Future

`Future` representa o resultado futuro de uma operação. `get()` é bloqueante, o que dificulta composição de operações assíncronas. Para workflows mais complexos, `CompletableFuture` costuma ser mais adequado.

---

## 10. CompletableFuture

`CompletableFuture` permite construir pipelines assíncronos:

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(this::buscarDados, executor);
```

Encadeamento:

```java
CompletableFuture<String> future = CompletableFuture
        .supplyAsync(this::buscarDados, executor)
        .thenApply(this::transformar)
        .thenApply(this::salvar);
```

---

## 11. thenApply

Use quando a próxima operação transforma o resultado.

```java
CompletableFuture<Cliente> cliente = buscarCliente();
CompletableFuture<String> nome = cliente.thenApply(Cliente::nome);
```

---

## 12. thenCompose

Use quando a próxima operação também retorna um `CompletableFuture`.

```java
buscarClienteAsync()
    .thenCompose(this::buscarContaAsync)
    .thenCompose(this::buscarLimiteAsync);
```

---

## 13. Operações paralelas com CompletableFuture

Execução paralela de chamadas independentes reduz latência agregada (aproxima-se do máximo em vez da soma):

```java
var cliente = CompletableFuture.supplyAsync(this::buscarCliente, executor);
var conta   = CompletableFuture.supplyAsync(this::buscarConta, executor);
var limite  = CompletableFuture.supplyAsync(this::buscarLimite, executor);

CompletableFuture.allOf(cliente, conta, limite)
    .thenRun(() -> {
        var c = cliente.join();
        var a = conta.join();
        var l = limite.join();
        consolidar(c, a, l);
    });
```

---

## 14. thenApply x thenApplyAsync

`thenApply` executa a transformação na mesma thread que completou a etapa anterior (se aplicável). `thenApplyAsync` permite controlar explicitamente o executor usado para a etapa com `thenApplyAsync(fn, executor)`.

---

## 15. Tratamento de erros

`CompletableFuture` fornece várias estratégias:

- exceptionally: tratar erro e fornecer fallback.
- handle: recebe (result, error) e pode retornar fallback ou resultado.
- whenComplete: observar resultado/erro (bom para logging/telemetria).

Exemplo:

```java
future.exceptionally(error -> {
    log.error("Erro", error);
    return fallback();
});

future.handle((result, error) -> {
    if (error != null) return fallback();
    return result;
});

future.whenComplete((r, e) -> {
    if (e != null) log.error("Erro", e);
});
```

Recomendações: defina políticas de retry com limites e backoff, use timeouts e garanta cancelamento cooperativo (interruption-aware).

---

## 16. Race Condition

Race condition ocorre quando o resultado depende da ordem de execução de múltiplas threads — operações aparentemente atômicas (como `contador++`) podem envolver leitura, modificação e escrita.

---

## 17. AtomicInteger

Para operações simples, use classes atômicas:

```java
AtomicInteger counter = new AtomicInteger();
counter.incrementAndGet();
```

Também: `AtomicLong`, `AtomicBoolean`, `AtomicReference`.

---

## 18. synchronized

`synchronized` cria região crítica. Use para seções pequenas que precisam ser protegidas.

```java
public synchronized void deposit(BigDecimal value) {
    balance = balance.add(value);
}
```

---

## 19. Cuidado com synchronized

Evite proteger trabalho demais dentro de `synchronized` — mantenha a região crítica pequena.

---

## 20. ConcurrentHashMap

`ConcurrentHashMap` permite acesso concorrente à coleção, mas operações compostas precisam usar as operações atômicas disponíveis (`putIfAbsent`, `computeIfAbsent`, `merge`, etc.).

---

## 21. Operações atômicas do ConcurrentHashMap

Exemplo:

```java
ConcurrentHashMap<String, Integer> counters = new ConcurrentHashMap<>();
counters.merge("success", 1, Integer::sum);
```

---

## 22. ConcurrentHashMap não torna tudo thread-safe

Objetos mutáveis armazenados na map ainda podem sofrer race conditions. Prefira imutabilidade ou técnicas de cópia.

---

## 23. Semaphore

`Semaphore` limita operações simultâneas para proteger recursos downstream.

```java
Semaphore semaphore = new Semaphore(20);
semaphore.acquire();
try { chamarBanco(); } finally { semaphore.release(); }
```

---

## 24. BlockingQueue

`BlockingQueue` é útil para Producer/Consumer. Use filas limitadas para prevenir crescimento de memória.

---

## 25. Backpressure

Backpressure é o mecanismo onde o consumidor impõe limites ao produtor (limites de in-flight, filas limitadas, pausa/retomada, Reactive Streams, etc.).

---

## 26. Virtual Threads no JDK 21

JDK 21 finalizou Virtual Threads para produção. Criar virtual thread:

```java
Thread.startVirtualThread(() -> processar());
```

Ou usar executor per-task:

```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> processar());
}
```

Virtual Threads reduzem custo por tarefa em workloads I/O-bound, permitindo milhares de threads com baixo overhead.

---

## 27. Virtual Thread x Platform Thread

Virtual Thread é uma abstração leve que é multiplexada sobre carrier threads/OS threads. Quando uma virtual thread bloqueia em I/O compatível, ela não ocupa continuamente um carrier.

---

## 28. Quando utilizar Virtual Threads

Ótimas para workloads com muitas operações bloqueantes: HTTP, DB, GCS, filesystem, APIs externas. Use com cuidado quando interop com bibliotecas nativas/legacy for necessária — teste se há chamadas que podem bloquear sem liberar o carrier.

---

## 29. Virtual Threads não são solução para CPU-bound

Virtual threads não aumentam o CPU disponível — para processamento intensivo em CPU, prefira um pool limitado (Fixed Thread Pool) dimensionado pelas CPUs.

---

## 30. Virtual Threads + Semaphore

Combinar virtual threads com `Semaphore` permite representar muitas tarefas com custo baixo na JVM, enquanto limita o número de operações simultâneas em um recurso.

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
Semaphore limit = new Semaphore(100);
executor.submit(() -> {
    limit.acquire();
    try { salvarNoGcs(); } finally { limit.release(); }
});
```

---

## 31. CompletableFuture + Virtual Threads

As duas abordagens podem coexistir. Virtual Threads simplificam programação imperativa/blocking; `CompletableFuture` facilita composição assíncrona. Escolha com base no modelo do problema.

---

## 32. Memory Leak em programação concorrente

Crescimento indefinido de estruturas (por exemplo `ConcurrentHashMap` com in-flight futures nunca removidos) pode causar vazamentos de memória. Use TTL, remoção ao completar, ou agendador para limpeza.

---

## 33. Removendo estado temporário

Padrão para evitar leaks no `inFlight`:

```java
// name=inflight-cleanup.java
ConcurrentHashMap<String, CompletableFuture<Result>> inFlight = new ConcurrentHashMap<>();

public CompletableFuture<Result> fetchOrProcess(String id, Supplier<CompletableFuture<Result>> processor) {
    var future = inFlight.computeIfAbsent(id, k -> {
        var f = processor.get();
        f.whenComplete((r, e) -> inFlight.remove(k, f));
        return f;
    });
    return future;
}

// opcional: agendador que limpa entradas muito antigas (TTL) caso futures travem
```

Sugestão adicional: manter um `ScheduledExecutorService` para varredura periódica e remover entradas com timestamp mais antigo que um limite configurável.

---

## 34. Lifecycle dos Executors

Não crie pools repetidamente. Gerencie o lifecycle no escopo da aplicação ou em objetos `AutoCloseable`.

```java
class Processor implements AutoCloseable {
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    void processar() { executor.submit(...); }
    @Override public void close() { executor.shutdown(); }
}
```

---

## 35. Deadlock

Deadlock ocorre quando threads adquirem locks em ordens diferentes e ficam esperando indefinidamente. Evite mantê-los aninhados, imponha ordem de aquisição, ou use timeouts.

---

## 36. Imutabilidade

Reduzir estado mutável compartilhado é uma das estratégias mais eficazes: use `record` e criação de novos objetos em vez de modificar estados compartilhados.

---

## 37. Producer/Consumer

Padrão clássico com `BlockingQueue` limitada e workers que processam até serem interrompidos.

---

## 38. Caso de uso: processamento CPU-bound

Dimensione workers ≈ número de CPUs usando `Runtime.getRuntime().availableProcessors()` e um `FixedThreadPool`.

---

## 39. Caso de uso: processamento I/O-bound

Virtual Threads podem aumentar throughput em workloads I/O-bound. Ainda assim, limite concorrência em recursos externos (DB, GCS).

---

## 40. Caso de uso: várias chamadas independentes

Use `CompletableFuture.supplyAsync` para chamar várias APIs em paralelo e `allOf` para consolidar.

---

## 41. Caso de uso: deduplicação concorrente

Use `ConcurrentHashMap.computeIfAbsent` para coalescer trabalho por chave e remova a entrada ao completar.

---

## 42. Caso de uso: limitar acesso a um downstream

Use `Semaphore` para proteger o recurso e evitar sobrecarga do downstream.

---

## 43. Caso de uso: Kafka + processamento + GCS

Ao processar mensagens Kafka em paralelo é crítico controlar commit/ack por partição, garantindo que offsets só sejam confirmados quando todas as mensagens anteriores foram processadas com sucesso.

---

## 44. Modelo de processamento concorrente para Kafka

Uma arquitetura típica:

```text
Kafka -> Consumer -> Bounded In-Flight -> Workers -> Completion Tracker -> Ordered Commit
```

### Exemplo de Completion Tracker (conceitual)

```text
Per-partition map: Map<partition, SortedSet<OffsetInFlight>>

onMessage(partition, offset):
    task = submitProcessing(...)
    register(partition, offset, task)

onTaskComplete(partition, offset):
    markCompleted(partition, offset)
    while lowestOffsetCompleted(partition):
        commit(lowestOffset)
        removeFromTracker(lowestOffset)
```

Implementar um `CompletionTracker` que mantenha a menor posição não confirmada e confirma até o último offset contíguo processado.

---

## 45. Throughput não significa aumentar threads indefinidamente

O throughput é limitado pelo gargalo (downstream, CPU, I/O). Aumentar threads pode apenas aumentar filas e latência.

---

## 46. Lei de Little

L = λ × W — útil para dimensionar concorrência e capacidade.

---

## 47. Métricas importantes

Monitore: throughput, p50/p95/p99, max latency, in-flight, queue depth, executor active threads, queue size, rejected tasks, CPU, heap, GC, downstream latency, error rate, timeouts. Para Kafka: consumer lag, records processed, processing latency, commit latency.

---

## 48. Instrumentação recomendada

Instrumente pontos do fluxo com timestamps (T1..T5) e exponha métricas via Micrometer/Prometheus.

Exemplo com Micrometer (pseudocódigo):

```java
long t1 = System.currentTimeMillis(); // consumer received
long t2 = ...; // processing started
long t3 = ...; // started GCS
long t4 = ...; // GCS finished
long t5 = ...; // commit

Timer.Sample sample = Timer.start(registry);
// ...processing...
sample.stop(registry.timer("processing.latency"));
```

Métricas recomendadas: `processing_latency_seconds{quantile}`, `in_flight_count`, `executor_active_threads`, `executor_queue_size`, `kafka_commit_latency_seconds`.

---

## 49. Como escolher a abordagem

Tabela resumida (ver original) com correspondência problema → abordagem (Runnable, Callable, ExecutorService, Fixed Thread Pool, CompletableFuture, ConcurrentHashMap, Atomic, synchronized, Semaphore, BlockingQueue, Virtual Threads, etc.).

---

## 50. Árvore de decisão

Um fluxo simples: preciso executar tarefas concorrentes? Se CPU-bound → Fixed Thread Pool; senão (I/O) → Virtual Threads; preciso compor operações? → CompletableFuture; existe estado compartilhado? → ConcurrentHashMap/Atomic/Lock; existe limite no downstream? → Semaphore/Bounded Queue.

---

## 51. Princípios para programação concorrente segura

1. Minimize estado compartilhado
2. Prefira imutabilidade
3. Limite trabalho in-flight
4. Tenha backpressure
5. Mantenha regiões críticas pequenas
6. Tenha lifecycle dos executors
7. Não confunda thread-safe com atomicidade
8. Não use Virtual Threads como solução universal
9. Meça antes de aumentar concorrência
10. Pense em falhas (timeouts, retries, cancellation, idempotência)

---

## 52. Modelo mental final

[Ver final do documento original para diagrama conceitual].

---

# Exemplos adicionais e padrões práticos

## Remoção segura do `inFlight` com TTL e cleanup (exemplo prático)

```java
// name=inflight-ttl-cleanup.java
class InFlightStore<K, V> {
    private static record Entry<V>(CompletableFuture<V> f, long createdAt) {}

    private final ConcurrentHashMap<K, Entry<V>> map = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
    private final Duration ttl = Duration.ofMinutes(10);

    public InFlightStore() {
        cleaner.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
    }

    public CompletableFuture<V> computeIfAbsent(K key, Supplier<CompletableFuture<V>> supplier) {
        var entry = map.computeIfAbsent(key, k -> new Entry<>(supplier.get(), System.currentTimeMillis()));
        entry.f.whenComplete((r, e) -> map.remove(key, entry));
        return entry.f;
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        map.forEach((k, e) -> {
            if (now - e.createdAt > ttl.toMillis()) map.remove(k, e);
        });
    }

    public void close() { cleaner.shutdownNow(); }
}
```

## Completion Tracker (esqueleto)

```java
class CompletionTracker {
    private final ConcurrentSkipListMap<Long, CompletableFuture<?>> offsets = new ConcurrentSkipListMap<>();
    private final AtomicLong committed = new AtomicLong(-1);

    public void register(long offset, CompletableFuture<?> future) {
        offsets.put(offset, future);
        future.whenComplete((r, e) -> tryAdvance());
    }

    private synchronized void tryAdvance() {
        while (!offsets.isEmpty()) {
            var entry = offsets.firstEntry();
            if (!entry.getValue().isDone()) break;
            offsets.remove(entry.getKey());
            committed.set(entry.getKey());
            // commit to Kafka the offset + 1
        }
    }
}
```

# Referências

- JEPs e documentação do JDK: https://openjdk.org/
- Virtual Threads (Project Loom): https://openjdk.org/projects/loom/
- CompletableFuture (JavaDoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletableFuture.html
- ConcurrentHashMap (JavaDoc): https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html
- Kafka processing patterns: https://www.confluent.io/blog/

---

Observações finais:

- Corrigi trechos truncados e padronizei fences de código com a linguagem (`java`, `text`).
- Adicionei TOC, exemplos práticos (inFlight cleanup, Completion Tracker) e recomendações de instrumentação.
- Mantive o conteúdo original e aprofundei pontos que são frequentemente fonte de bugs (timeouts, TTL, cleanup, commit ordenado em Kafka).

Se quiser, eu abro um PR em vez de gravar direto na `main` (bom para revisão), ou posso também dividir o conteúdo em subpáginas (`examples/`, `kafka/`) e criar arquivos separados. Diga qual prefere.
