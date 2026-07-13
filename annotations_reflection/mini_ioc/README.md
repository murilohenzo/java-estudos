# mini_ioc — Mini container de Beans (estilo Spring) em Java 21

> IoC/DI **puro** com Annotations + Reflection — sem Spring.  
> Mostra como um `@Component` vira bean, como o **registry** controla o ciclo de vida  
> e como fazer isso de forma **thread-safe**.

---

## Ideia (mapa mental Spring → este módulo)

| Spring Boot | Este exemplo |
|---|---|
| `@Component` / `@Service` | `@Component` |
| `@Autowired` / `@Inject` | `@Inject` (construtor ou campo) |
| `@Scope("singleton"\|"prototype")` | `BeanScope.SINGLETON` / `PROTOTYPE` |
| `@PostConstruct` / `@PreDestroy` | mesmos nomes |
| `ApplicationContext` | `ApplicationContext` |
| `DefaultListableBeanFactory` / singleton cache | `BeanRegistry` |

```
register(@Component classes)
        │
        ▼
  BeanDefinition (record imutável)
        │
        ▼
  BeanRegistry.freeze()     ← metadados não mudam mais
        │
        ▼
  refresh() → cria SINGLETONs
        │     instantiate → @Inject deps → @PostConstruct
        ▼
  getBean()  (singleton: cache | prototype: nova instância)
        │
        ▼
  close() → @PreDestroy → limpa registry
```

---

## Estrutura

```
mini_ioc/src/br/com/murilo/ioc/
├── annotation/
│   ├── Component.java      # marca o bean
│   ├── BeanScope.java      # SINGLETON | PROTOTYPE
│   ├── Inject.java         # DI
│   ├── PostConstruct.java
│   └── PreDestroy.java
├── container/
│   ├── BeanDefinition.java # metadados imutáveis
│   ├── BeanRegistry.java   # encapsula cache + thread-safety
│   ├── BeanCreationException.java
│   └── ApplicationContext.java
└── demo/
    ├── UserRepository.java
    ├── UserService.java
    ├── RequestIdGenerator.java  # prototype
    └── MiniIocDemo.java
```

---

## Ciclo de vida do bean

1. **Definition** — classe anotada vira `BeanDefinition(name, type, scope)`
2. **Instantiation** — reflection no construtor (`@Inject` ou único / no-args)
3. **Population** — campos `@Inject` (se houver)
4. **Initialization** — métodos `@PostConstruct`
5. **Ready** — disponível via `getBean`
6. **Destruction** (só singletons no `close`) — `@PreDestroy`

Prototype: passos 2–4 a **cada** `getBean`; não recebe `@PreDestroy` automático do container (igual ideia do Spring: ciclo do prototype é do cliente).

---

## Thread-safety — boas práticas aplicadas

| Prática | Onde |
|---|---|
| `ConcurrentHashMap` para definições e singletons | `BeanRegistry` |
| `putIfAbsent` no registro (falha se nome/tipo duplicado) | `register` |
| **Freeze** após refresh — registry de metadados imutável | `freeze()` |
| Double-checked locking na criação de singleton | `getOrCreateSingleton` |
| Monitor dedicado (`singletonCreationLock`), não `this` | evita lock externo acidental |
| `synchronized` é **reentrante** — DI aninhada (A→B) na mesma thread ok | criação de deps |
| **Não** usar `computeIfAbsent` para criar bean com deps | factory publicaria no mesmo map (proibido pelo contrato do CHM) |
| Detecção de ciclo com `ThreadLocal<Map>` | `beansUnderConstruction` |
| Shutdown idempotente (`AtomicBoolean`) | `close()` |
| Estado mutável interno do demo com `ConcurrentHashMap` / `AtomicInteger` | `UserRepository`, prototype counter |
| Virtual Threads no stress test de `getBean` | `MiniIocDemo` |

---

## Uso rápido

```java
try (var ctx = new ApplicationContext()
        .register(UserRepository.class, UserService.class, RequestIdGenerator.class)
        .refresh()) {

    UserService service = ctx.getBean(UserService.class);
    service.greet("1");

    // prototype → instâncias distintas
    var a = ctx.getBean(RequestIdGenerator.class);
    var b = ctx.getBean(RequestIdGenerator.class);
    assert a != b;
} // @PreDestroy nos singletons
```

Bean com DI por construtor:

```java
@Component
public class UserService {
    private final UserRepository repository;

    @Inject
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

---

## Como executar (JDK 21+)

```bash
cd annotations_reflection/mini_ioc/src
javac br/com/murilo/ioc/annotation/*.java \
      br/com/murilo/ioc/container/*.java \
      br/com/murilo/ioc/demo/*.java
java br.com.murilo.ioc.demo.MiniIocDemo
```

---

## Limitações (propositalmente simples)

- Sem scan de classpath (registro explícito)
- Sem proxies AOP / `@Transactional`
- Sem qualifiers (`@Primary`, `@Qualifier`)
- Sem injeção por nome em parâmetros
- Circular dependency: detectada e rejeitada (sem early reference do Spring)

Serve para entender o **mecanismo** por trás do Spring, não para substituí-lo.
