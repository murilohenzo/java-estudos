# Programação Concorrente no JDK 21

## 1. Objetivo

Este documento apresenta os principais conceitos e ferramentas de **programação concorrente no Java/JDK 21**, com foco em aplicações backend, processamento de eventos, sistemas distribuídos e pipelines de alto throughput.

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

# 2. Concorrência x Paralelismo

Antes das APIs, é importante separar dois conceitos.

## Concorrência

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

## Paralelismo

Paralelismo ocorre quando tarefas são executadas realmente ao mesmo tempo em diferentes recursos de processamento.

```text
CPU 1 ─── Task A ─────────────

CPU 2 ─── Task B ─────────────

CPU 3 ─── Task C ─────────────
```

### Regra prática

Para CPU-bound:

```text
mais threads ≠ necessariamente mais throughput
```

Para I/O-bound:

```text
mais concorrência pode aumentar bastante o throughput
```

Exemplos de I/O-bound:

* HTTP
* banco de dados
* GCS/S3
* filesystem
* chamadas a APIs externas

Exemplos de CPU-bound:

* criptografia pesada
* compressão
* parsing muito pesado
* processamento matemático
* transformação de grandes volumes em memória

---

# 3. Thread

A abstração básica é `Thread`.

```java
Thread thread = new Thread(() -> {
    System.out.println("Executando");
});

thread.start();
```

É importante observar:

```java
thread.run();
```

não cria uma nova execução concorrente.

Já:

```java
thread.start();
```

inicia a thread.

## Quando utilizar

Criar threads manualmente é útil principalmente para:

* entender os fundamentos;
* casos muito específicos;
* infraestrutura de baixo nível.

Em aplicações backend, normalmente é preferível utilizar `ExecutorService`.

---

# 4. Runnable

`Runnable` representa uma tarefa sem retorno.

```java
Runnable task = () -> {
    processarEvento();
};
```

Pode ser submetido a um executor:

```java
executor.execute(task);
```

ou:

```java
executor.submit(task);
```

A diferença é que `submit()` produz um `Future`.

---

# 5. Callable

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

Modelo:

```text
Callable
   |
   v
ExecutorService
   |
   v
Future<T>
   |
   v
Resultado
```

---

# 6. ExecutorService

`ExecutorService` separa a definição da tarefa do gerenciamento das threads.

```java
ExecutorService executor =
        Executors.newFixedThreadPool(8);

executor.submit(() -> {
    processarEvento();
});
```

Em vez de:

```text
criar Thread
executar
destruir Thread
```

temos:

```text
                    ExecutorService
                          |
            +-------------+-------------+
            |             |             |
         Worker 1       Worker 2      Worker 3
            |             |             |
          Task A        Task B        Task C
```

## Benefícios

* reutilização de threads;
* controle de concorrência;
* gerenciamento de lifecycle;
* filas internas;
* isolamento de workloads.

---

# 7. Fixed Thread Pool

Exemplo:

```java
ExecutorService executor =
        Executors.newFixedThreadPool(8);
```

Significa que no máximo 8 threads executarão tarefas simultaneamente.

Imagine 100 tarefas:

```text
Thread 1 → Task 1
Thread 2 → Task 2
Thread 3 → Task 3
...
Thread 8 → Task 8

Fila:
Task 9
Task 10
Task 11
...
Task 100
```

Quando uma thread termina:

```text
Thread 3 terminou Task 3
        |
        v
executa Task 9
```

## Quando utilizar

Muito útil para CPU-bound ou quando é necessário impor um limite rígido de concorrência.

Exemplo:

```java
int workers =
        Runtime.getRuntime().availableProcessors();

ExecutorService executor =
        Executors.newFixedThreadPool(workers);
```

---

# 8. Cuidado com o tamanho do Thread Pool

Não existe uma regra universal como:

```text
CPU cores × 2
```

O dimensionamento depende do workload.

Para CPU-bound:

```text
workers ≈ número de CPUs disponíveis
```

Para I/O-bound:

```text
workers pode ser maior
```

porque as threads passam parte significativa do tempo esperando.

Porém, aumentar indiscriminadamente pode gerar:

* context switching;
* contenção;
* aumento de memória;
* aumento de latência;
* saturação do downstream.

---

# 9. Future

`Future` representa o resultado futuro de uma operação.

```java
Future<String> future =
        executor.submit(() -> buscarDados());
```

Você pode consultar:

```java
future.isDone();
```

ou:

```java
future.get();
```

O problema de `Future` é que `get()` é bloqueante.

```java
String resultado = future.get();
```

Isso limita a composição de operações assíncronas.

Para workflows mais complexos, `CompletableFuture` é geralmente mais adequado.

---

# 10. CompletableFuture

`CompletableFuture` permite construir pipelines assíncronos.

```java
CompletableFuture<String> future =
        CompletableFuture.supplyAsync(
                () -> buscarDados(),
                executor
        );
```

Podemos encadear:

```java
CompletableFuture<String> future =
        CompletableFuture
                .supplyAsync(
                        this::buscarDados,
                        executor
                )
                .thenApply(this::transformar)
                .thenApply(this::salvar);
```

Fluxo:

```text
buscarDados
     |
     v
transformar
     |
     v
salvar
```

---

# 11. thenApply

Use quando a próxima operação transforma o resultado.

```java
CompletableFuture<Cliente> cliente =
        buscarCliente();

CompletableFuture<String> nome =
        cliente.thenApply(Cliente::nome);
```

Conceitualmente:

```text
Future<A>
   |
   | thenApply
   v
Future<B>
```

---

# 12. thenCompose

Use quando a próxima operação também retorna um `CompletableFuture`.

```java
buscarClienteAsync()
        .thenCompose(this::buscarContaAsync)
        .thenCompose(this::buscarLimiteAsync);
```

Sem `thenCompose`, poderíamos acabar com:

```text
CompletableFuture<CompletableFuture<Resultado>>
```

Com `thenCompose`:

```text
CompletableFuture<Resultado>
```

É muito utilizado para criar pipelines assíncronos dependentes.

---

# 13. Operações paralelas com CompletableFuture

Imagine três chamadas independentes:

```text
             +-- Cliente
             |
Evento ------+-- Conta
             |
             +-- Limite
```

Podemos executar em paralelo:

```java
CompletableFuture<Cliente> cliente =
        CompletableFuture.supplyAsync(
                this::buscarCliente,
                executor
        );

CompletableFuture<Conta> conta =
        CompletableFuture.supplyAsync(
                this::buscarConta,
                executor
        );

CompletableFuture<Limite> limite =
        CompletableFuture.supplyAsync(
                this::buscarLimite,
                executor
        );
```

Depois:

```java
CompletableFuture.allOf(
        cliente,
        conta,
        limite
).thenRun(() -> {

    var c = cliente.join();
    var a = conta.join();
    var l = limite.join();

    consolidar(c, a, l);
});
```

A latência pode se aproximar da operação mais lenta, em vez da soma de todas.

Sequencial:

```text
100ms + 200ms + 300ms = 600ms
```

Paralelo:

```text
max(100, 200, 300) = ~300ms
```

---

# 14. thenApply x thenApplyAsync

`thenApply`:

```java
future.thenApply(this::processar);
```

`thenApplyAsync`:

```java
future.thenApplyAsync(
        this::processar,
        executor
);
```

Quando é importante controlar explicitamente o executor utilizado pela etapa, prefira `thenApplyAsync`.

Isso é especialmente importante em aplicações com diferentes workloads.

Por exemplo:

```text
Executor CPU
Executor I/O
Executor Kafka
Executor processamento pesado
```

Evite misturar indiscriminadamente tudo no mesmo executor.

---

# 15. Tratamento de erros

## exceptionally

```java
future.exceptionally(error -> {
    log.error("Erro", error);
    return fallback();
});
```

## handle

```java
future.handle((result, error) -> {

    if (error != null) {
        return fallback();
    }

    return result;
});
```

## whenComplete

```java
future.whenComplete((result, error) -> {

    if (error != null) {
        log.error("Erro", error);
    }
});
```

Use `whenComplete` principalmente para observabilidade ou efeitos colaterais.

---

# 16. Race Condition

Uma race condition ocorre quando o resultado depende da ordem de execução de múltiplas threads.

Exemplo:

```java
int contador = 0;

contador++;
```

Apesar de parecer uma operação única, conceitualmente envolve:

```text
READ
  |
ADD
  |
WRITE
```

Duas threads podem executar:

```text
Thread A             Thread B

READ 10              READ 10
ADD                  ADD
WRITE 11             WRITE 11
```

Resultado:

```text
11
```

quando o esperado seria:

```text
12
```

---

# 17. AtomicInteger

Para operações simples, podemos usar:

```java
AtomicInteger counter =
        new AtomicInteger();

counter.incrementAndGet();
```

Também existem:

```text
AtomicLong
AtomicBoolean
AtomicReference
```

Exemplo:

```java
AtomicLong processed =
        new AtomicLong();

processed.incrementAndGet();
```

Isso é útil para:

* métricas;
* contadores;
* flags;
* referências compartilhadas.

---

# 18. synchronized

`synchronized` cria uma região de exclusão mútua.

```java
public synchronized void incrementar() {
    contador++;
}
```

Ou:

```java
synchronized (lock) {
    atualizarEstado();
}
```

Somente uma thread poderá executar a região protegida por aquele lock.

## Quando utilizar

Use quando existe uma seção crítica pequena que precisa ser protegida.

Exemplo:

```java
class Account {

    private BigDecimal balance;

    public synchronized void deposit(
            BigDecimal value) {

        balance = balance.add(value);
    }
}
```

---

# 19. Cuidado com synchronized

O problema:

```java
synchronized
```

não é ruim por si só.

O problema é proteger trabalho demais.

Evite:

```java
synchronized (lock) {

    chamadaHttp();
    chamadaBanco();
    salvarArquivo();
}
```

Você pode acabar transformando uma aplicação altamente concorrente em uma execução praticamente serial.

Prefira:

```java
var dados = chamadaHttp();

synchronized (lock) {
    atualizarEstado(dados);
}
```

A região crítica fica pequena.

---

# 20. ConcurrentHashMap

Para estado compartilhado em uma coleção:

```java
ConcurrentHashMap<String, Evento> events =
        new ConcurrentHashMap<>();
```

Podemos executar:

```java
events.put(id, evento);

Evento evento = events.get(id);
```

com múltiplas threads.

Porém, operações compostas precisam de atenção.

Evite:

```java
if (!map.containsKey(key)) {
    map.put(key, value);
}
```

Prefira:

```java
map.putIfAbsent(key, value);
```

---

# 21. Operações atômicas do ConcurrentHashMap

Algumas das operações mais importantes:

```java
putIfAbsent()
compute()
computeIfAbsent()
computeIfPresent()
merge()
replace()
```

Exemplo:

```java
ConcurrentHashMap<String, Integer> counters =
        new ConcurrentHashMap<>();

counters.merge(
        "success",
        1,
        Integer::sum
);
```

Isso evita implementar manualmente uma sequência não atômica.

---

# 22. ConcurrentHashMap não torna tudo thread-safe

Considere:

```java
ConcurrentHashMap<String, Pedido> pedidos;
```

Isso não significa que:

```java
pedidos.get(id).incrementar();
```

seja necessariamente seguro.

Se `Pedido` for mutável e várias threads modificarem o mesmo objeto, ainda existe possibilidade de race condition.

Uma abordagem preferível é utilizar objetos imutáveis.

Exemplo:

```java
record Resultado(
        String id,
        Status status,
        Instant processedAt
) {}
```

---

# 23. Semaphore

`Semaphore` controla quantas operações podem acontecer simultaneamente.

```java
Semaphore semaphore =
        new Semaphore(20);
```

Uso:

```java
semaphore.acquire();

try {
    chamarBanco();
} finally {
    semaphore.release();
}
```

Isso significa:

```text
1000 tarefas
      |
      v
Semaphore(20)
      |
      v
máximo 20 operações
      |
      v
Banco
```

## Caso de uso

Imagine:

```text
Aplicação = 5000 tarefas concorrentes
Banco = suporta 20 operações simultâneas
```

O `Semaphore` protege o downstream.

---

# 24. BlockingQueue

Uma `BlockingQueue` é útil para implementar Producer/Consumer.

```java
BlockingQueue<Evento> queue =
        new ArrayBlockingQueue<>(1000);
```

Producer:

```java
queue.put(evento);
```

Consumer:

```java
Evento evento =
        queue.take();
```

Arquitetura:

```text
Producer
   |
   v
BlockingQueue
   |
   +---- Worker 1
   +---- Worker 2
   +---- Worker 3
   +---- Worker 4
```

A capacidade limitada é importante.

Uma fila infinita pode transformar um problema de throughput em um problema de memória.

---

# 25. Backpressure

Backpressure significa:

> O consumidor consegue sinalizar ou impor um limite para impedir que o produtor gere trabalho além da capacidade sustentável.

Exemplo:

```text
Entrada
1000 msg/s
   |
   v
Aplicação
300 msg/s
```

Sem controle:

```text
fila → cresce continuamente
```

Com backpressure:

```text
Entrada
   |
   v
Limite
   |
   v
300/s
```

Ferramentas possíveis:

* `Semaphore`
* `BlockingQueue` limitada
* limites de in-flight
* rate limiting
* pausa/retomada do consumer
* Reactive Streams
* mecanismos de backpressure do framework

---

# 26. Virtual Threads no JDK 21

JDK 21 introduziu Virtual Threads como recurso finalizado para produção.

Criação:

```java
Thread.startVirtualThread(() -> {
    processar();
});
```

Ou:

```java
try (ExecutorService executor =
         Executors.newVirtualThreadPerTaskExecutor()) {

    executor.submit(() -> processar());
}
```

A principal vantagem é permitir grande quantidade de tarefas concorrentes com custo muito menor que criar uma quantidade equivalente de platform threads.

---

# 27. Virtual Thread x Platform Thread

Tradicionalmente:

```text
Java Thread
     |
     v
OS Thread
```

Com Virtual Threads:

```text
Virtual Thread
      |
      v
Carrier Thread
      |
      v
OS Thread
```

Quando a virtual thread fica bloqueada em operações compatíveis com o modelo:

```text
Virtual Thread
      |
      v
esperando I/O
      |
      X
não precisa ocupar continuamente o carrier
```

Outra virtual thread pode utilizar o recurso de execução.

---

# 28. Quando utilizar Virtual Threads

Excelente para workloads com muitas operações bloqueantes:

```text
HTTP
DB
GCS
Filesystem
APIs externas
```

Exemplo:

```java
try (var executor =
         Executors.newVirtualThreadPerTaskExecutor()) {

    executor.submit(() -> {
        var response = httpClient.send(request);
        salvar(response);
    });
}
```

---

# 29. Virtual Threads não são solução para CPU-bound

Considere:

```java
executor.submit(() -> {

    for (long i = 0;
         i < 10_000_000_000L;
         i++) {

        calcular(i);
    }
});
```

Se o problema é CPU:

```text
8 CPUs
```

não significa que:

```text
1000 virtual threads
```

produzirão 1000 unidades de processamento paralelo.

O CPU continua sendo o recurso limitante.

Para CPU-bound, um pool limitado costuma ser mais apropriado.

---

# 30. Virtual Threads + Semaphore

Uma combinação muito poderosa é:

```java
ExecutorService executor =
        Executors.newVirtualThreadPerTaskExecutor();

Semaphore limit =
        new Semaphore(100);
```

Execução:

```java
executor.submit(() -> {

    limit.acquire();

    try {
        salvarNoGcs();
    } finally {
        limit.release();
    }
});
```

Você pode ter muitas tarefas representadas por Virtual Threads, mas somente 100 operações simultâneas no recurso protegido.

---

# 31. CompletableFuture + Virtual Threads

Não é obrigatório utilizar os dois.

Virtual Thread:

```java
executor.submit(() -> processar());
```

já permite programação relativamente simples e bloqueante.

`CompletableFuture` é útil quando existe necessidade de compor operações.

Exemplo:

```java
CompletableFuture
        .supplyAsync(
                this::buscarCliente,
                executor
        )
        .thenCompose(
                this::buscarContaAsync
        )
        .thenApply(
                this::processar
        );
```

A escolha deve ser baseada no modelo do problema, não na ideia de que "mais async é melhor".

---

# 32. Memory Leak em programação concorrente

Um dos erros mais comuns é criar estruturas concorrentes que nunca são limpas.

Exemplo:

```java
ConcurrentHashMap<String, CompletableFuture<?>> inFlight =
        new ConcurrentHashMap<>();
```

Se adicionarmos continuamente:

```text
event-1
event-2
event-3
...
event-1.000.000
```

e nunca removermos:

```text
Heap
 |
 +-- event-1
 +-- event-2
 +-- event-3
 ...
 +-- event-1.000.000
```

temos crescimento indefinido.

---

# 33. Removendo estado temporário

Um padrão útil:

```java
CompletableFuture<Result> future =
        inFlight.computeIfAbsent(
                id,
                key -> processarAsync(evento)
        );

future.whenComplete((result, error) ->
        inFlight.remove(id, future)
);

return future;
```

O:

```java
remove(id, future)
```

é importante.

Ele evita remover um novo processamento que eventualmente tenha sido associado à mesma chave.

---

# 34. Lifecycle dos Executors

Evite:

```java
public void processar() {

    ExecutorService executor =
            Executors.newFixedThreadPool(8);

    executor.submit(...);
}
```

Se esse método for chamado milhares de vezes, você pode criar milhares de pools.

Prefira um lifecycle controlado:

```java
class Processor implements AutoCloseable {

    private final ExecutorService executor =
            Executors.newFixedThreadPool(8);

    void processar() {
        executor.submit(...);
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
```

Ou gerencie o executor pelo lifecycle da aplicação.

---

# 35. Deadlock

Deadlock ocorre quando threads ficam esperando umas pelas outras indefinidamente.

Exemplo conceitual:

```text
Thread A
  |
  | possui Lock 1
  |
  +---- espera Lock 2


Thread B
  |
  | possui Lock 2
  |
  +---- espera Lock 1
```

Resultado:

```text
A espera B
B espera A
```

Nunca termina.

## Como evitar

* reduzir quantidade de locks;
* manter ordem consistente de aquisição;
* evitar locks aninhados;
* manter regiões críticas pequenas;
* usar timeout quando apropriado.

---

# 36. Imutabilidade

Uma das melhores estratégias para reduzir problemas concorrentes é reduzir estado mutável compartilhado.

Prefira:

```java
record EventResult(
        String id,
        String status
) {}
```

a objetos compartilhados que diversas threads modificam.

Em termos arquiteturais:

```text
Input
  |
  v
Transformação
  |
  v
Novo objeto
  |
  v
Output
```

é mais fácil de raciocinar do que:

```text
Thread A ─┐
Thread B ─┼──> objeto mutável compartilhado
Thread C ─┘
```

---

# 37. Producer/Consumer

Um padrão clássico:

```text
                Producer
                   |
                   v
             +-----------+
             |   Queue   |
             +-----------+
              |    |   |
              v    v   v
             W1    W2  W3
```

Exemplo:

```java
BlockingQueue<Evento> queue =
        new ArrayBlockingQueue<>(500);
```

Producer:

```java
queue.put(evento);
```

Workers:

```java
while (!Thread.currentThread().isInterrupted()) {

    Evento evento = queue.take();

    processar(evento);
}
```

A fila limitada atua como mecanismo de backpressure.

---

# 38. Caso de uso: processamento CPU-bound

Exemplo:

```text
Kafka
  |
  v
Consumer
  |
  v
CPU-heavy processing
  |
  v
Output
```

Uma abordagem:

```java
int workers =
        Runtime.getRuntime().availableProcessors();

ExecutorService executor =
        Executors.newFixedThreadPool(workers);
```

Por quê?

Porque o gargalo é CPU.

---

# 39. Caso de uso: processamento I/O-bound

Exemplo:

```text
Kafka
  |
  v
Processamento
  |
  +---- HTTP
  |
  +---- GCS
  |
  +---- DB
```

Aqui Virtual Threads podem ser muito interessantes:

```java
try (var executor =
         Executors.newVirtualThreadPerTaskExecutor()) {

    executor.submit(() -> {
        processar();
        salvarNoGcs();
    });
}
```

Adicionar concorrência pode aumentar o throughput porque as tarefas passam boa parte do tempo esperando I/O.

---

# 40. Caso de uso: várias chamadas independentes

Imagine:

```text
Evento
  |
  +-- API A
  +-- API B
  +-- API C
```

Com `CompletableFuture`:

```java
var a =
    CompletableFuture.supplyAsync(
        this::callA,
        executor
    );

var b =
    CompletableFuture.supplyAsync(
        this::callB,
        executor
    );

var c =
    CompletableFuture.supplyAsync(
        this::callC,
        executor
    );

CompletableFuture.allOf(a, b, c)
        .thenRun(this::consolidar);
```

Excelente quando as operações são independentes.

---

# 41. Caso de uso: deduplicação concorrente

Imagine vários eventos com o mesmo ID.

```java
ConcurrentHashMap<String, CompletableFuture<Result>>
        inFlight = new ConcurrentHashMap<>();
```

Podemos utilizar:

```java
inFlight.computeIfAbsent(
        event.id(),
        id -> processarAsync(event)
);
```

Assim, várias requisições podem compartilhar o mesmo processamento.

Depois:

```java
future.whenComplete(
        (result, error) ->
                inFlight.remove(id, future)
);
```

O estado é temporário e possui lifecycle definido.

---

# 42. Caso de uso: limitar acesso a um downstream

Imagine:

```text
Aplicação
5000 tarefas
      |
      v
Banco
máximo sustentável = 50 operações
```

Use:

```java
Semaphore semaphore =
        new Semaphore(50);
```

E:

```java
semaphore.acquire();

try {
    consultarBanco();
} finally {
    semaphore.release();
}
```

Isso protege o recurso.

---

# 43. Caso de uso: Kafka + processamento + GCS

Considere:

```text
Kafka
  |
  v
processa evento
  |
  v
salva GCS
  |
  v
commit/ack
```

O ponto fundamental é:

```text
ACK/commit
     |
     v
somente após sucesso do processamento
e persistência necessária
```

Com processamento paralelo, porém, existe um problema adicional: **ordenação dos offsets por partição**.

Imagine:

```text
Offset 100 → processando
Offset 101 → terminou
Offset 102 → terminou
```

Não é correto simplesmente assumir que o offset 102 pode ser confirmado como progresso seguro da partição enquanto o 100 ainda não terminou.

Por isso, processamento concorrente de Kafka exige controle de:

* offsets concluídos;
* ordem;
* commit;
* retries;
* falhas;
* reprocessamento;
* idempotência.

---

# 44. Modelo de processamento concorrente para Kafka

Uma arquitetura possível:

```text
                     Kafka
                       |
                       v
                    Consumer
                       |
                       v
               Bounded In-Flight
                       |
                 +-----+-----+
                 |           |
                 v           v
             Worker 1    Worker 2
                 |           |
                 v           v
             processa      processa
                 |           |
                 v           v
                GCS         GCS
                 |           |
                 +-----+-----+
                       |
                       v
                Completion Tracker
                       |
                       v
                 Ordered Commit
```

Para workloads I/O-bound:

```text
Consumer
   |
   v
Virtual Threads
   |
   v
GCS
```

pode ser uma arquitetura interessante.

Mas o limite de concorrência deve ser controlado.

---

# 45. Throughput não significa aumentar threads indefinidamente

Suponha:

```text
Kafka = 1000 msg/s
Processamento = 600 msg/s
GCS = 200 msg/s
```

O throughput final será limitado pelo gargalo.

```text
Kafka       1000
Process      600
GCS          200
              |
              v
           ~200/s
```

Colocar:

```text
8 → 32 → 64 → 128 threads
```

não necessariamente aumenta o throughput.

Pode apenas aumentar:

* fila;
* memória;
* contenção;
* latência;
* pressão no GCS.

---

# 46. Lei de Little

Uma relação extremamente útil para dimensionamento:

```text
L = λ × W
```

Onde:

* `L` = trabalho médio em andamento;
* `λ` = throughput;
* `W` = tempo médio de processamento.

Exemplo:

```text
Throughput = 400 msg/s
Latência = 2 s
```

Então:

```text
L = 400 × 2
L = 800
```

Aproximadamente 800 operações precisam estar in-flight para sustentar esse throughput naquele cenário.

Essa relação é muito útil para dimensionar:

* workers;
* concorrência;
* filas;
* capacidade de downstream;
* memória.

---

# 47. Métricas importantes

Em sistemas concorrentes, não monitore apenas CPU.

Métricas importantes:

```text
Throughput
p50
p95
p99
Maximum latency
In-flight
Queue depth
Executor active threads
Executor queue size
Rejected tasks
CPU
Heap
GC
Allocation rate
Downstream latency
Error rate
Timeouts
```

Para Kafka:

```text
Consumer lag
Records consumed
Records processed
Processing latency
Commit latency
Retry count
DLQ/DLT
```

---

# 48. Instrumentação recomendada

Um processamento:

```text
Kafka event
     |
     v
T1 = consumer recebeu
     |
     v
T2 = iniciou processamento
     |
     v
T3 = iniciou GCS
     |
     v
T4 = GCS terminou
     |
     v
T5 = commit
```

Permite medir:

```text
Kafka waiting time
    = T1 - eventTimestamp

Queue time
    = T2 - T1

Processing time
    = T3 - T2

GCS latency
    = T4 - T3

Commit latency
    = T5 - T4

End-to-end
    = T5 - eventTimestamp
```

Isso é muito mais útil para troubleshooting do que observar apenas uma latência agregada.

---

# 49. Como escolher a abordagem

| Problema                            | Abordagem                                     |
| ----------------------------------- | --------------------------------------------- |
| Executar tarefa                     | `Runnable`                                    |
| Tarefa com retorno                  | `Callable`                                    |
| Gerenciar workers                   | `ExecutorService`                             |
| CPU-bound                           | Fixed Thread Pool                             |
| Pipeline assíncrono                 | `CompletableFuture`                           |
| Paralelizar operações independentes | `CompletableFuture.allOf()`                   |
| Compartilhar mapa                   | `ConcurrentHashMap`                           |
| Contador compartilhado              | `AtomicInteger/Long`                          |
| Proteger seção crítica              | `synchronized`                                |
| Lock avançado                       | `ReentrantLock`                               |
| Limitar concorrência                | `Semaphore`                                   |
| Producer/Consumer                   | `BlockingQueue`                               |
| Backpressure                        | Queue limitada / Semaphore / Reactive Streams |
| Muitas operações I/O                | Virtual Threads                               |
| CPU-intensive                       | Platform threads limitadas                    |
| Evitar estado compartilhado         | Imutabilidade                                 |
| Coordenar tarefas relacionadas      | Structured Concurrency                        |
| Evitar crescimento de memória       | Bounded concurrency + lifecycle               |

---

# 50. Árvore de decisão

Uma forma simples de decidir:

```text
Preciso executar tarefas concorrentes?
             |
             v
           Sim
             |
             v
O workload é predominantemente CPU?
       /             \
     Sim             Não
      |               |
      v               v
Fixed Thread      Muitas operações
Pool              bloqueantes/I/O?
                       |
                       v
                     Sim
                       |
                       v
                Virtual Threads
```

Depois:

```text
Preciso compor operações assíncronas?
          |
          +-- Sim → CompletableFuture
```

E:

```text
Existe estado compartilhado?
          |
          +-- Sim → ConcurrentHashMap / Atomic / Lock
```

E:

```text
Existe limite no downstream?
          |
          +-- Sim → Semaphore / Bounded Queue
```

---

# 51. Princípios para programação concorrente segura

## 1. Minimize estado compartilhado

Quanto menos estado compartilhado, menor a possibilidade de race condition.

## 2. Prefira imutabilidade

Use:

```java
record
```

quando possível.

## 3. Limite o trabalho in-flight

Nunca permita crescimento ilimitado de tarefas.

## 4. Tenha backpressure

A capacidade do produtor não pode crescer indefinidamente em relação ao consumidor.

## 5. Mantenha regiões críticas pequenas

Evite:

```java
synchronized {
    HTTP();
    DB();
    GCS();
}
```

## 6. Tenha lifecycle dos executors

Executor precisa ser criado, monitorado e encerrado corretamente.

## 7. Não confunda thread-safe com atomicidade da operação

Uma coleção thread-safe não significa que qualquer sequência de operações sobre ela seja atomicamente segura.

## 8. Não use Virtual Threads como solução universal

Elas são excelentes para I/O-bound, mas não removem limites de CPU ou downstream.

## 9. Meça antes de aumentar concorrência

Aumentar workers sem entender o gargalo pode piorar o sistema.

## 10. Pense em falhas

Concorrência aumenta a complexidade de:

* timeout;
* retry;
* cancellation;
* duplicate processing;
* ordering;
* commit;
* idempotência.

---

# 52. Modelo mental final

Uma boa forma de pensar em programação concorrente é:

```text
                     TAREFAS
                        |
                        v
                  Como executar?
                        |
            +-----------+-----------+
            |                       |
            v                       v
        Executor              Virtual Thread
            |
            v
       Quantidade?
            |
            v
        Concurrency
            |
            v
       Há limite?
            |
            v
        Semaphore
            |
            v
     Estado compartilhado?
            |
       +----+----+
       |         |
       v         v
    Atomic     CHM
       |         |
       +----+----+
            |
            v
       Precisa proteger
       região crítica?
            |
            v
       synchronized/Lock
            |
            v
       Operações assíncronas?
            |
            v
    CompletableFuture
            |
            v
       Backpressure
            |
            v
        Observabilidade
            |
            v
       Throughput sustentável
```

A principal mudança de mentalidade é deixar de pensar em **"quantas threads eu coloco?"** e passar a pensar em:

> **Qual é o recurso limitante? Qual concorrência ele suporta? Quantas operações posso manter in-flight? Como coordeno as tarefas? Como garanto visibilidade/atomicidade do estado? Como aplico backpressure? E como comprovo isso através de métricas?**

Esse modelo é especialmente importante em arquiteturas de eventos, porque o objetivo não é simplesmente processar mais mensagens, mas **maximizar throughput sustentável sem aumentar indefinidamente lag, latência, memória ou pressão nos downstreams**.
