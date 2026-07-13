# annotations_reflection — Annotations e Reflection API

> Dois conceitos profundamente interligados: **annotations** definem metadados em tempo
> de compilação; **reflection** os lê e age sobre eles em tempo de execução.
> Juntos, formam a base de frameworks como Spring, Hibernate e JUnit.

---

## Submódulos

| Diretório | Conteúdo |
|---|---|
| [`annotations_exceptions/`](./annotations_exceptions/README.md) | Custom annotations (`@Retention`, `@Target`), hierarquia de exceções de domínio, `try-with-resources` |
| [`reflection/`](./reflection/README.md) | `java.lang.reflect`, `AuditProcessor`, DDL generation via `@ClassTable`/`@Column` |
| [`mini_ioc/`](./mini_ioc/README.md) | Mini container de beans estilo Spring — `@Component`, DI, registry thread-safe, ciclo de vida (JDK 21) |
| [`mini_ioc_jdk25/`](./mini_ioc_jdk25/README.md) | Mesmo IoC em JDK 25 — `ScopedValue` no lugar de `ThreadLocal` (detecção de ciclo) |
| [`reflect_metadata/`](./reflect_metadata/README.md) | Comparativo Java ↔ `reflect-metadata` (TS 5.3.3) + espelho de API |

---

## Relação entre os módulos

```
annotations_exceptions/
  @Anotacao              ← define a annotation
  @ClassTable, @Column   ← annotations para mapeamento ORM-like
         │
         │  (lidas em runtime por)
         ▼
reflection/
  AuditProcessor         ← inspeciona campos com @Auditable
  TableReflections       ← gera DDL a partir de @ClassTable + @Column
                           (simulação de como o Hibernate funciona internamente)
         │
         │  (mesmo princípio aplicado a DI)
         ▼
mini_ioc/
  @Component + @Inject   ← metadados de bean
  ApplicationContext     ← reflection instancia, injeta e controla lifecycle
  BeanRegistry           ← encapsula singletons de forma thread-safe
         │
         │  (mesma ideia, JDK 25)
         ▼
mini_ioc_jdk25/
  ApplicationContext     ← ScopedValue (JEP 506) no lugar de ThreadLocal
         │
         │  (camada de metadados — paralelo Nest/TS)
         ▼
reflect_metadata/
  typescript/            ← mini reflect-metadata (TS 5.3.3)
  java/MetadataReflect   ← mesma API em Java (comparativo)
```

---

## Como frameworks usam esses conceitos

| Framework | Annotation | Reflection |
|---|---|---|
| **Hibernate/JPA** | `@Entity`, `@Column`, `@Id` | Lê anotações para mapear tabelas |
| **Spring** | `@Component`, `@Autowired` | Escaneia classpath, injeta dependências |
| **JUnit 5** | `@Test`, `@BeforeEach` | Descobre e executa métodos de teste |
| **Jackson** | `@JsonProperty` | Serializa/deserializa campos |
| **Este repo (`mini_ioc`)** | `@Component`, `@Inject` | IoC manual — bean registry + lifecycle |
| **NestJS / TypeORM** | Decorators + `reflect-metadata` | Lê `design:paramtypes` em runtime |
| **Este repo (`reflect_metadata`)** | `defineMetadata` (TS/Java) | Comparativo educacional com a lib TS |

---

## Referência relacionada

- [`mini_ioc/`](./mini_ioc/README.md) — analogia prática ao bean container do Spring Boot (JDK 21)  
- [`mini_ioc_jdk25/`](./mini_ioc_jdk25/README.md) — mesma analogia com Scoped Values (JDK 25)  
- [`reflect_metadata/`](./reflect_metadata/README.md) — comparativo com [reflect-metadata](https://github.com/microsoft/reflect-metadata) (TS 5.3.3)  
- `orm/` — anotações Jakarta EE (`@Entity`, `@OneToMany`) em produção
