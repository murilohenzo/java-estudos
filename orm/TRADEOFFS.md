# Data Access Layer — Trade-offs Técnicos

> **Módulo:** `mod31` — JPA / Hibernate / JDBI  
> **Java:** 21 (LTS) | **Hibernate:** 6.4 | **JDBI:** 3.45 | **HikariCP:** 5.1  
> **Banco:** PostgreSQL 14+

---

## Arquitetura do Módulo

```
mod31/
├── config/
│   ├── PersistenceConfig.java   ← EMF singleton (JPA bootstrap)
│   ├── HibernateConfig.java     ← SessionFactory singleton (Hibernate native)
│   └── JdbiConfig.java          ← JDBI + HikariCP pool
│
├── domain/
│   ├── Course.java              ← Entidade JPA (mutável, gerenciada pelo ORM)
│   ├── Registration.java        ← Entidade JPA com @ManyToOne + @PrePersist
│   ├── CourseRecord.java        ← JDK 21 record (imutável, read model)
│   └── enums/Status.java        ← Enum com factory method (switch expression JDK 21)
│
└── repository/
    ├── ICourseRepository.java          ← Contrato comum (interface)
    ├── jpa/CourseJpaRepository.java    ← Pure JPA (jakarta.persistence.*)
    ├── hibernate/CourseHibernateRepository.java ← Hibernate Session API
    └── jdbi/
        ├── CourseDao.java              ← SQL Object interface declarativa
        └── CourseJdbiRepository.java   ← JDBI fluent + SQL Object
```

---

## 1. Pure JPA (`CourseJpaRepository`)

### Quando usar
- Aplicações que precisam de **portabilidade** entre providers ORM (Hibernate, EclipseLink, OpenJPA)
- Projetos que usam **Spring Data JPA** ou **Quarkus Panache** (abstraem sobre JPA)
- CRUD padrão com entidades bem definidas e relacionamentos gerenciados

### Como funciona
```java
// Contexto de persistência: EntityManager
em.persist(entity);     // INSERT — entidade passa a "managed"
em.merge(entity);       // UPDATE — copia estado de detached para managed
em.find(Course.class, id); // SELECT por PK — usa L1 cache
em.getReference(Course.class, id); // Proxy sem SELECT — DELETE eficiente
em.createQuery("FROM Course c", Course.class).getResultList(); // JPQL
```

### Prós
| Vantagem | Detalhe |
|----------|---------|
| **Portabilidade** | API padronizada JPA 3.1 (Jakarta EE 10) |
| **Change tracking** | Dirty checking automático no flush |
| **Lazy loading** | `@OneToMany(fetch=LAZY)` carrega coleções on-demand |
| **L1 Cache** | Dentro de uma transação, mesmo ID → mesmo objeto |
| **JPQL** | SQL orientado a objetos, independente do banco |

### Contras
| Desvantagem | Detalhe |
|-------------|---------|
| **N+1 Query** | `findAll()` + acesso a `course.getRegistrations()` = N+1 SELECTs |
| **SQL opaco** | SQL gerado pode ser sub-ótimo sem `FETCH JOIN` explícito |
| **Overhead de entidade** | Carrega colunas desnecessárias se não usar projeções |
| **Sem StatelessSession** | Batch processing consome memória do L1 cache |

### Mitigação de N+1
```java
// Ruim: N+1
courses.forEach(c -> c.getRegistrations().size()); // SELECT para cada Course

// Bom: JOIN FETCH em uma query
em.createQuery("FROM Course c JOIN FETCH c.registrations", Course.class)

// Melhor para read models: projeção com Constructor Expression
em.createQuery("SELECT new CourseRecord(c.id, c.code, c.name, c.description) FROM Course c")
```

---

## 2. Hibernate Session API (`CourseHibernateRepository`)

### Quando usar
- Precisa de funcionalidades **exclusivas do Hibernate** não expostas pelo JPA
- **Batch processing** de grande volume via `StatelessSession`
- Filtros dinâmicos multi-tenant (`@Filter`)
- Interceptores e eventos de sessão customizados

### Funcionalidades exclusivas

#### StatelessSession — Zero overhead para batch
```java
// Session normal: cada persist() entra no L1 cache → OutOfMemoryError com 100k rows
try (var stateless = sessionFactory.openStatelessSession()) {
    for (var entity : largeBatch) {
        stateless.insert(entity);  // direto ao banco, sem cache
    }
}
```

| | `Session` | `StatelessSession` |
|---|---|---|
| L1 Cache | ✅ | ❌ |
| Change tracking | ✅ | ❌ |
| Cascades | ✅ | ❌ |
| `@PrePersist` hooks | ✅ | ❌ |
| Performance (bulk) | Moderada | Alta |
| Memória (bulk) | Alta (cresce com entidades) | Constante |

#### HQL Bulk Mutation — DELETE/UPDATE sem SELECT
```java
// Deleta sem carregar entidade — ideal para deleções em massa
session.createMutationQuery("DELETE FROM Course WHERE code LIKE :prefix")
       .setParameter("prefix", "TEMP-%")
       .executeUpdate();

// UPDATE em massa sem dirty check por entidade
session.createMutationQuery("UPDATE Course SET name = CONCAT('[ARCHIVED] ', name) WHERE id IN :ids")
       .setParameter("ids", ids)
       .executeUpdate();
```

### Prós vs JPA
- Acesso a `StatelessSession` para ETL e migrations
- `Session.get()` vs `Session.load()` (proxy sem SQL imediato)
- Configuração programática via `Configuration` (sem XML)
- `session.createNativeMutationQuery()` para SQL nativo com rollback gerenciado

### Contras
- **Acoplamento ao Hibernate** — troca de ORM exige reescrita
- Mais verboso que JPA para operações simples
- Requer conhecimento profundo das internals do Hibernate

---

## 3. JDBI 3 (`CourseJdbiRepository`)

### Quando usar
- **Read models** e APIs REST onde imutabilidade é preferível
- Queries **analíticas complexas** com múltiplos JOINs e agregações
- **Microserviços leves** sem container JEE/Spring
- **Bulk insert/update** com performance máxima
- Quando o DBA quer revisar e controlar o SQL exato

### Dois modos de uso

#### SQL Object (declarativo)
```java
@RegisterConstructorMapper(CourseRecord.class)
public interface CourseDao {

    @SqlQuery("SELECT id, code, name, description FROM TB_COURSE WHERE id = :id")
    Optional<CourseRecord> findById(@Bind("id") long id);

    @SqlUpdate("INSERT INTO TB_COURSE (code, name, description) VALUES (:code, :name, :description)")
    @GetGeneratedKeys("id")
    long insert(@Bind("code") String code, @Bind("name") String name, @Bind("description") String description);

    @SqlBatch("INSERT INTO TB_COURSE (code, name, description) VALUES (:code, :name, :description)")
    int[] batchInsert(@Bind("code") List<String> codes, ...);
}
```

#### Fluent API (imperativo — queries dinâmicas)
```java
jdbi.withHandle(h ->
    h.createQuery("SELECT id, code, name, description FROM TB_COURSE WHERE LOWER(name) LIKE :p")
     .bind("p", "%" + pattern.toLowerCase() + "%")
     .mapToBean(CourseRecord.class)
     .list()
);
```

### JDK 21 Record + JDBI
```java
// Record é mapeado via construtor canônico pelo ConstructorMapper
public record CourseRecord(Long id, String code, String name, String description) {}

// JDBI mapeia cada coluna para o componente correspondente
@RegisterConstructorMapper(CourseRecord.class)
Optional<CourseRecord> findById(@Bind("id") long id);
```

### Modelo de transação
```java
// Read — sem transação explícita
jdbi.withHandle(h -> h.attach(CourseDao.class).findAll());

// Write — transação com rollback automático em exceção
jdbi.inTransaction(h -> {
    h.attach(CourseDao.class).insert(...);
    return generatedId;
});

// Write void
jdbi.useTransaction(h -> h.attach(CourseDao.class).deleteById(id));
```

### Prós
| Vantagem | Detalhe |
|----------|---------|
| **SQL explícito** | Zero surpresas; fácil auditoria; DBA-friendly |
| **Records JDK 21** | Mapeamento direto para tipos imutáveis |
| **Batch nativo** | `@SqlBatch` usa `PreparedStatement.addBatch()` |
| **Sem N+1** | Sem lazy loading — você controla cada query |
| **Startup rápido** | Sem scanning de entidades, sem proxy generation |
| **Jar menor** | Dependência ~500KB vs Hibernate ~10MB |

### Contras
| Desvantagem | Detalhe |
|-------------|---------|
| **Schema manual** | DDL não é gerenciado automaticamente (use Flyway/Liquibase) |
| **Sem lazy loading** | Relacionamentos precisam de JOIN explícito |
| **Sem L2 cache** | Cache deve ser implementado na camada de aplicação |
| **SQL boilerplate** | SELECT/INSERT explícito para cada operação |
| **Sem cascade** | Operações em entidades relacionadas são manuais |

---

## 4. Comparativo Geral

| Critério | Pure JPA | Hibernate Nativo | JDBI |
|----------|----------|------------------|------|
| **Portabilidade ORM** | ✅ Alta | ❌ Baixa | ✅ N/A (sem ORM) |
| **Controle de SQL** | ⚠️ Médio | ⚠️ Médio | ✅ Total |
| **Lazy loading** | ✅ | ✅ | ❌ |
| **Cache L1** | ✅ | ✅ | ❌ |
| **Cache L2** | ✅ | ✅ | ❌ |
| **Batch processing** | ⚠️ | ✅ (StatelessSession) | ✅ (@SqlBatch) |
| **Records JDK 21** | Via projeção | Via projeção | ✅ Nativo |
| **Schema automático** | ✅ | ✅ | ❌ |
| **Curva de aprendizado** | Média | Alta | Baixa |
| **Performance (CRUD simples)** | Boa | Boa | Excelente |
| **Performance (queries complexas)** | Ruim sem tuning | Boa | Excelente |
| **Startup time** | Lento (scanning) | Lento | Rápido |

---

## 5. Estratégia CQRS com as três abordagens

Em arquiteturas **CQRS (Command Query Responsibility Segregation)**:

```
COMMAND SIDE (Write Model)
  ├── CourseJpaRepository     — CRUD com change tracking e cascades
  └── CourseHibernateRepository — Bulk mutations e batch processing

QUERY SIDE (Read Model)
  └── CourseJdbiRepository    — Projeções SQL otimizadas retornando CourseRecord
```

Esta combinação é comum em sistemas de alta escala:
- ORM gerencia o modelo de escrita (entidades ricas, relacionamentos, cascades)
- JDBI serve o modelo de leitura (projeções denormalizadas, queries otimizadas, imutabilidade)

---

## 6. JDK 21 — Recursos Utilizados

| Feature | Onde | Desde JDK |
|---------|------|-----------|
| `record` | `CourseRecord`, `RegistrationRecord` | 16 (GA) |
| Compact constructor | `CourseRecord { ... }` | 16 (GA) |
| Pattern matching `instanceof` | `Course.equals()` | 16 (GA) |
| Text blocks | SQL queries em `CourseDao`, JPQL em repositórios | 15 (GA) |
| `switch` expression | `Status.fromString()` | 14 (GA) |
| `Stream.toList()` | `CourseJdbiRepository.findAll()` | 16 (GA) |
| `String.formatted()` | `toString()` nas entidades | 15 (GA) |
| `var` | variáveis locais em todos os repositórios | 10 (GA) |

---

## 7. HikariCP — Configuração de Produção Recomendada

```java
cfg.setMaximumPoolSize(10);    // núcleos_CPU * 2 + spindles
cfg.setMinimumIdle(2);         // conexões sempre prontas
cfg.setConnectionTimeout(30_000);  // 30s para obter conexão do pool
cfg.setIdleTimeout(600_000);   // 10min antes de remover conexão ociosa
cfg.setMaxLifetime(1_800_000); // 30min — rotaciona conexões (evita timeout do servidor)
cfg.setAutoCommit(false);      // transações explícitas sempre
```

**Regra de tamanho de pool:**  
Para PostgreSQL: `pool_size = (número_de_cores * 2) + número_de_discos_spindle`  
Em produção com 4 cores e SSD: `pool_size = (4 * 2) + 1 = 9` → arredonde para 10.

---

## 8. Decisão de Escolha — Fluxograma

```
Você precisa de portabilidade de ORM?
├── SIM → Pure JPA (jakarta.persistence.*)
└── NÃO →
    Precisa de StatelessSession, filtros dinâmicos ou bulk HQL?
    ├── SIM → Hibernate Nativo
    └── NÃO →
        É um read model, projeção ou query analítica complexa?
        ├── SIM → JDBI + CourseRecord (JDK 21 record)
        └── NÃO →
            É operação de escrita com entidades relacionadas?
            ├── SIM → Pure JPA (cascades, lazy loading)
            └── NÃO → JDBI (CRUD simples sem overhead ORM)
```
