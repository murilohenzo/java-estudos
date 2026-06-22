# Mod 31 — ORM: Pure JPA · Hibernate Native · JDBI 3

> **Três estratégias de persistência** com trade-offs documentados para guiar decisões arquiteturais.  
> Java 21 · Hibernate 6.4 · JDBI 3.45 · HikariCP 5.1 · JUnit 5 · PostgreSQL

---

## Visão Geral

Este módulo implementa o mesmo CRUD de Cursos com **três abordagens distintas** de acesso a dados,
cada uma demonstrando padrões, vantagens e limitações específicas.

```
Abstração ▲│                    JPA/Hibernate
           │    ─────────────────── ORM (alto nível)
           │
           │         JDBI 3
           │    ─────────── SQL-first (médio nível)
           │
           │    JDBC puro (mod29)
           │    ────────────────── SQL direto (baixo nível)
           └──────────────────────────────────────────► Controle de SQL
```

---

## Estrutura do Módulo

```
mod31/
├── pom.xml                      # Java 21 · Hibernate 6 · JDBI 3 · HikariCP · JUnit 5
├── TRADEOFFS.md                 # Comparativo técnico detalhado com tabelas e fluxograma
│
└── src/main/java/br/com/murilo/
    ├── config/
    │   ├── PersistenceConfig.java     # EntityManagerFactory singleton (JPA)
    │   ├── HibernateConfig.java       # SessionFactory singleton (Hibernate)
    │   └── JdbiConfig.java            # JDBI + HikariCP pool
    ├── domain/
    │   ├── Course.java                # Entidade JPA: @Entity, @OneToMany LAZY
    │   ├── Registration.java          # Entidade JPA: @ManyToOne, @PrePersist, @Enumerated(STRING)
    │   ├── CourseRecord.java          # JDK 21 record: imutável, read model para JDBI
    │   └── enums/Status.java          # Enum com switch expression (JDK 21)
    └── repository/
        ├── ICourseRepository.java                   # Interface comum (7 métodos)
        ├── jpa/CourseJpaRepository.java             # Pure JPA (EntityManager + JPQL)
        ├── hibernate/CourseHibernateRepository.java # Hibernate Session API nativa
        └── jdbi/
            ├── CourseDao.java                       # SQL Object (@SqlQuery/@SqlUpdate)
            └── CourseJdbiRepository.java            # JDBI Fluent + SQL Object

src/test/java/br/com/murilo/modules/course/
    ├── CourseTest.java           # Smoke test JPA
    ├── CourseJpaTest.java        # Integration tests JPA
    ├── CourseHibernateTest.java  # Integration tests Hibernate (incl. StatelessSession)
    └── CourseJdbiTest.java       # Integration tests JDBI (incl. batch insert)
```

---

## Configuração

### Banco de dados

```sql
CREATE DATABASE vendas_online;
-- Usuário: postgres / Senha: admin (padrão dev — altere em produção)

-- Tabelas criadas automaticamente pelo Hibernate DDL auto = update
-- Sequences criadas automaticamente pelo Hibernate
```

### Executar testes

```bash
cd mod31
mvn test
```

---

## JDK 21 — Recursos utilizados

### `record` — Tipo imutável para read models

```java
public record CourseRecord(Long id, String code, String name, String description) {

    // Compact constructor — validações antes dos campos serem atribuídos
    public CourseRecord {
        if (code == null || code.isBlank())
            throw new IllegalArgumentException("Code must not be blank");
        code = code.toUpperCase().strip();  // normalização automática
    }

    // Copy-with pattern — records são imutáveis, "atualização" gera nova instância
    public CourseRecord withId(Long id) {
        return new CourseRecord(id, this.code, this.name, this.description);
    }
}
```

### Pattern matching `instanceof`

```java
@Override
public boolean equals(Object o) {
    if (!(o instanceof Course c)) return false;  // JDK 16+ — cast automático
    return Objects.equals(code, c.code);
}
```

### `switch` expression com arrow

```java
public static Optional<Status> fromString(String value) {
    return switch (value.toUpperCase()) {
        case "ACTIVE"    -> Optional.of(ACTIVE);
        case "INACTIVE"  -> Optional.of(INACTIVE);
        case "PENDING"   -> Optional.of(PENDING);
        case "CANCELLED" -> Optional.of(CANCELLED);
        default          -> Optional.empty();
    };
}
```

### Text blocks para SQL

```java
@SqlQuery("""
        SELECT id, code, name, description
        FROM TB_COURSE
        WHERE UPPER(code) = UPPER(:code)
        """)
Optional<CourseRecord> findByCode(@Bind("code") String code);
```

---

## Implementação 1: Pure JPA

```java
// Usa apenas jakarta.persistence.* — portável entre providers ORM
public class CourseJpaRepository implements ICourseRepository {

    @Override
    public Course create(Course course) {
        return executeInTransaction(em -> {
            em.persist(course);
            em.flush();  // força INSERT e retorna ID gerado
            return course;
        });
    }

    @Override
    public List<CourseRecord> findAllAsRecords() {
        return withEntityManager(em -> {
            // Constructor Expression — projeta para record sem change-tracking
            var jpql = """
                    SELECT new br.com.murilo.domain.CourseRecord(c.id, c.code, c.name, c.description)
                    FROM Course c ORDER BY c.name
                    """;
            return em.createQuery(jpql, CourseRecord.class).getResultList();
        });
    }
}
```

---

## Implementação 2: Hibernate Session API

```java
// Usa org.hibernate.Session — acesso a funcionalidades exclusivas do Hibernate
public class CourseHibernateRepository implements ICourseRepository {

    // Bulk operations com StatelessSession — zero overhead de L1 cache
    public void bulkInsert(List<Course> courses) {
        try (var stateless = HibernateConfig.getSessionFactory().openStatelessSession()) {
            var tx = stateless.beginTransaction();
            courses.forEach(stateless::insert);  // sem cache, sem dirty check
            tx.commit();
        }
    }

    // HQL bulk DELETE sem carregar entidades em memória
    @Override
    public void deleteById(Long id) {
        executeInTransactionVoid(session ->
            session.createMutationQuery("DELETE FROM Course WHERE id = :id")
                   .setParameter("id", id)
                   .executeUpdate()  // 1 DELETE sem SELECT anterior
        );
    }
}
```

---

## Implementação 3: JDBI 3

```java
// SQL-first — você escreve SQL, JDBI mapeia para objetos
@RegisterConstructorMapper(CourseRecord.class)
public interface CourseDao {

    @SqlUpdate("""
            INSERT INTO TB_COURSE (code, name, description)
            VALUES (:code, :name, :description)
            """)
    @GetGeneratedKeys("id")
    long insert(@Bind("code") String code, @Bind("name") String name, @Bind("description") String description);

    // Batch insert — uma única roundtrip JDBC para N registros
    @SqlBatch("""
            INSERT INTO TB_COURSE (code, name, description) VALUES (:code, :name, :description)
            """)
    int[] batchInsert(@Bind("code") List<String> codes,
                      @Bind("name") List<String> names,
                      @Bind("description") List<String> descriptions);
}
```

---

## Comparativo Rápido

| | **Pure JPA** | **Hibernate Native** | **JDBI** |
|---|---|---|---|
| Portabilidade | ✅ Alta | ❌ Hibernate only | ✅ N/A |
| SQL explícito | ❌ | ❌ | ✅ |
| Records JDK 21 | Via projeção | Via projeção | ✅ Nativo |
| Lazy loading | ✅ | ✅ | ❌ |
| Batch sem L1 cache | ❌ | ✅ StatelessSession | ✅ |
| Startup | Lento | Lento | Rápido |

> Para análise detalhada com fluxograma de decisão, veja [TRADEOFFS.md](./TRADEOFFS.md).

---

## Entidades JPA

### Course — `@OneToMany` com `@Index`

```java
@Entity
@Table(name = "TB_COURSE",
       indexes = {@Index(name = "idx_course_code", columnList = "code", unique = true)})
public class Course {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "course_seq")
    @SequenceGenerator(name="course_seq", sequenceName="sq_course", allocationSize=50)
    private Long id;

    @OneToMany(mappedBy="course", cascade=ALL, fetch=LAZY, orphanRemoval=true)
    private List<Registration> registrations = new ArrayList<>();
}
```

### Registration — `@ManyToOne LAZY` + `@PrePersist`

```java
@Entity
@Table(name = "TB_REGISTRATION")
public class Registration {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)  // armazena "ACTIVE" ao invés do ordinal 0
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "created_at", updatable = false)  // imutável após INSERT
    private Instant createdAt;

    @PrePersist
    private void onPrePersist() { this.createdAt = Instant.now(); }
}
```

---

## Dependências (pom.xml)

```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.4.4.Final</version>      <!-- Jakarta EE, JDK 21 compatível -->
</dependency>
<dependency>
    <groupId>org.jdbi</groupId>
    <artifactId>jdbi3-core</artifactId>
    <version>3.45.1</version>
</dependency>
<dependency>
    <groupId>org.jdbi</groupId>
    <artifactId>jdbi3-sqlobject</artifactId>
    <version>3.45.1</version>
</dependency>
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>
```

---

## Referências

- [TRADEOFFS.md](./TRADEOFFS.md) — Análise técnica detalhada: JPA vs Hibernate vs JDBI
- [mod26\_27\_28](../mod26_27_28/README.md) — SQL fundamentos (DDL, DML, Joins, Índices)
- [mod29](../mod29/README.md) — JDBC puro (base de toda a stack de persistência)
