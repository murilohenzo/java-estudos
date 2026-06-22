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

---

## Relação entre os dois módulos

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
```

---

## Como frameworks usam esses conceitos

| Framework | Annotation | Reflection |
|---|---|---|
| **Hibernate/JPA** | `@Entity`, `@Column`, `@Id` | Lê anotações para mapear tabelas |
| **Spring** | `@Component`, `@Autowired` | Escaneia classpath, injeta dependências |
| **JUnit 5** | `@Test`, `@BeforeEach` | Descobre e executa métodos de teste |
| **Jackson** | `@JsonProperty` | Serializa/deserializa campos |

---

## Referência relacionada

Ver `orm/` para ver anotações Jakarta EE (`@Entity`, `@OneToMany`) em produção,
e como o Hibernate as lê via reflection internamente.
