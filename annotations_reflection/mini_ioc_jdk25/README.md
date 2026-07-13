# mini_ioc_jdk25 — Mini IoC com Scoped Values (JDK 25)

> Mesmo desenho do [`mini_ioc/`](../mini_ioc/README.md) (beans estilo Spring),  
> trocando **`ThreadLocal` → `ScopedValue` (JEP 506, GA no Java 25)** na detecção de dependência circular.

---

## Diferença central

| | `mini_ioc` (JDK 21) | `mini_ioc_jdk25` |
|---|---|---|
| Contexto de criação | `ThreadLocal<Map<String,Object>>` | `ScopedValue<Set<String>>` |
| Mutabilidade | Map mutável (`put` / `remove`) | Set **imutável** + **rebind** a cada nível |
| Lifetime | Até `remove()` manual | Lexical — acaba ao sair do `call`/`run` |
| Virtual Threads | Herança frágil / cara (InheritableThreadLocal) | Herda o binding automaticamente |
| Vazamento | Risco se esquecer `remove` | Sem `remove` — escopo sintático |

### Padrão usado (idiomático JEP 506)

```java
private static final ScopedValue<Set<String>> BEANS_UNDER_CONSTRUCTION =
        ScopedValue.newInstance();

private Object createAndInitialize(String name) throws Exception {
    Set<String> parent = BEANS_UNDER_CONSTRUCTION.isBound()
            ? BEANS_UNDER_CONSTRUCTION.get()
            : Set.of();

    if (parent.contains(name)) {
        throw new BeanCreationException("dependência circular em '" + name + "'");
    }

    Set<String> current = Set.copyOf(new HashSet<>(parent) {{ add(name); }});

    return ScopedValue.where(BEANS_UNDER_CONSTRUCTION, current).call(() -> {
        // instantiate + @Inject + @PostConstruct
        // getBean(dep) aninhado vê o Set ampliado
        return instance;
    });
}
```

Cadeia `UserService` → `UserRepository`:

```
where({userService})
  └─ where({userService, userRepository})  ← cria repository
  └─ finaliza userService
// fora do call: isBound() == false
```

---

## Estrutura

```
mini_ioc_jdk25/src/br/com/murilo/ioc25/
├── annotation/     # iguais ao mini_ioc (@Component, @Inject, …)
├── container/
│   ├── BeanRegistry.java        # ConcurrentHashMap + DCL (igual)
│   └── ApplicationContext.java  # ScopedValue no lugar de ThreadLocal
└── demo/
    └── MiniIocDemo.java
```

Package: `br.com.murilo.ioc25` (não conflita com `ioc` do JDK 21).

---

## Como executar (JDK 25+)

```bash
cd annotations_reflection/mini_ioc_jdk25/src
javac --release 25 br/com/murilo/ioc25/annotation/*.java \
      br/com/murilo/ioc25/container/*.java \
      br/com/murilo/ioc25/demo/*.java
java br.com.murilo.ioc25.demo.MiniIocDemo
```

---

## Quando preferir ScopedValue neste cenário

- Contexto de **request / criação** com lifetime claro (como o stack de beans em construção)
- Código que já usa **Virtual Threads** e precisa propagar contexto sem `InheritableThreadLocal`
- Evitar estado mutável thread-local esquecido após exceções

`BeanRegistry` (cache de singletons) continua com `ConcurrentHashMap` + double-checked locking — Scoped Values não substituem um cache compartilhado entre requests; substituem o **contexto por cadeia de chamada**.

Ver também: [`java25` — Scoped Values](../../java25/README.md#jep-506--scoped-values).
