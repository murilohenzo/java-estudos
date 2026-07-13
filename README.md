# java-estudos

> Jornada completa de Java: de coleções e OOP até JPA, Hibernate, JDBI e arquitetura de software.  
> **Java 11 → 25** | **Design Patterns** | **Testes** | **Persistência** | **Engenharia de Software**

---

## Estrutura do Projeto

```
java-estudos/
│
├── collections/                  # Java Collections Framework (List, Queue, Map, Tree)
│   ├── lineares/                 #   ArrayList, ArrayDeque, PriorityQueue, LinkedList
│   └── mapas/                    #   Array multidimensional, HashMap, TreeMap, LinkedHashMap
│
├── oop_fundamentos/              # OOP: Interfaces, Classes Abstratas, Composição, Polimorfismo
│
├── projeto01_crud_swing/         # Projeto 01 — CRUD de Clientes (Swing, Layered Architecture)
│
├── design_patterns/              # Design Patterns GOF (10 padrões: Creational/Structural/Behavioral)
│
├── generics/                     # Generics: bounds, wildcards, PECS, Generic Repository
│
├── annotations_reflection/       # Annotations + Reflection API (base dos frameworks)
│   ├── annotations_exceptions/   #   Custom annotations, hierarquia de exceções
│   ├── reflection/               #   java.lang.reflect, AuditProcessor, DDL generation
│   ├── mini_ioc/                 #   Mini Spring-like IoC (JDK 21, ThreadLocal)
│   ├── mini_ioc_jdk25/           #   Mesmo IoC com Scoped Values (JDK 25)
│   └── reflect_metadata/         #   Comparativo reflect-metadata (TS 5.3.3) ↔ Java
│
├── programacao_funcional/        # Programação Funcional (lambdas → streams)
│   ├── lambdas/                  #   Lambda, java.util.function, method references, closures
│   └── streams/                  #   Stream pipeline, flatMap, Collectors, parallel streams
│
├── testes/                       # Testes Automatizados
│   ├── junit4/                   #   JUnit 4 fundamentos, Assert.*, lifecycle
│   └── service_dao/              #   Test Doubles, Suite, constructor injection
│
├── projeto02_use_cases/          # Projeto 02 — Use Case Pattern + Maven + CI/CD (GitHub Actions)
├── projeto02_generic_dao/        # Projeto 02 v1 — Generic DAO + Reflection + @TipoChave
│
├── sql_fundamentos/              # SQL: DDL, DML, JOINs, Subqueries, Views, Triggers, Índices
├── jdbc/                         # JDBC: DriverManager, PreparedStatement, ConnectionFactory, DAO
│
├── orm/                          # ORM — Pure JPA · Hibernate Native · JDBI 3 · HikariCP · JDK 21
│
├── java21/                       # Java 21 LTS — Virtual Threads, Pattern Matching, Sequenced Collections
├── java25/                       # Java 25 LTS — Scoped Values, Flexible Constructors, Compact Source, Module Import
│
└── guia_engenharia/              # Guias transversais: Big O, Carga Cognitiva, Coesão/Acoplamento
```

---

## Navegação

### Fundamentos da Linguagem

| Módulo | Tópico | Conceitos-chave |
|--------|--------|-----------------|
| [collections](./collections/README.md) | Collections Framework | List, Queue, Map · O(1) vs O(log n) · Comparator |
| [oop_fundamentos](./oop_fundamentos/README.md) | OOP | Interface, Abstract Class, Composição · Repository Pattern · Optional |
| [generics](./generics/README.md) | Generics | Bounds, Wildcards, PECS · Generic Repository · Type Erasure |
| [annotations_reflection](./annotations_reflection/README.md) | Annotations + Reflection | Custom annotations · Mini IoC · reflect-metadata (TS 5.3.3) |
| [programacao_funcional](./programacao_funcional/README.md) | Lambdas + Streams | OO vs funcional · diamante · `java.util.function` · Streams |

### Design e Arquitetura

| Módulo | Tópico | Conceitos-chave |
|--------|--------|-----------------|
| [design_patterns](./design_patterns/README.md) | Design Patterns GOF | Singleton, Factory, Builder, Adapter, Facade, Observer, Strategy |
| [projeto01_crud_swing](./projeto01_crud_swing/README.md) | Projeto 01 | Layered Architecture · Use Cases · Repository · Swing |
| [projeto02_use_cases](./projeto02_use_cases/README.md) | Projeto 02 | Clean Use Cases · Domain Exceptions · Maven · CI/CD |
| [projeto02_generic_dao](./projeto02_generic_dao/README.md) | Projeto 02 v1 | Generic DAO · Reflection · `@TipoChave` · Singleton Map |

### Testes

| Módulo | Tópico | Conceitos-chave |
|--------|--------|-----------------|
| [testes](./testes/README.md) | Testes Automatizados | JUnit 4 · Test Doubles · Mock/Stub/Fake · Suite · Constructor DI |

### Persistência

| Módulo | Tópico | Conceitos-chave |
|--------|--------|-----------------|
| [sql_fundamentos](./sql_fundamentos/README.md) | SQL | DDL/DML · JOINs · Subqueries · Views · Triggers · EXPLAIN ANALYZE |
| [jdbc](./jdbc/README.md) | JDBC | DriverManager · PreparedStatement · ConnectionFactory · DAO |
| [orm](./orm/README.md) | ORM | Pure JPA · Hibernate Session API · JDBI 3 · HikariCP · JDK 21 |

### Java LTS e Engenharia

| Módulo | Tópico | Conceitos-chave |
|--------|--------|-----------------|
| [java21](./java21/README.md) | Java 21 LTS | Virtual Threads · Pattern Matching switch · Record Patterns · Sequenced Collections |
| [java25](./java25/README.md) | Java 25 LTS | 18 JEPs · Scoped Values · Flexible Constructors · KDF · JFR/AOT · Previews |
| [guia_engenharia](./guia_engenharia/README.md) | Boas Práticas | Big O · Carga Cognitiva · Alta Coesão · Baixo Acoplamento |

---

## Evolução Arquitetural

O projeto acompanha a evolução natural de um desenvolvedor Java, revisitando
o mesmo domínio (Customer/Client CRUD) com arquiteturas progressivamente mais sofisticadas:

```
collections          →  Lógica procedural com coleções
oop_fundamentos      →  Separação por interfaces e herança
projeto01_crud_swing →  Layered Architecture: domain → repository → useCase → UI
generics             →  Generic Repository com Template Method
projeto02_generic_dao→  Generic DAO com Reflection (@TipoChave)
projeto02_use_cases  →  Clean Use Cases + Custom Exceptions + CI/CD
jdbc                 →  Raw SQL com ConnectionFactory + DAO
orm                  →  Pure JPA · Hibernate Session API · JDBI 3 · HikariCP
java21               →  Virtual Threads · Modern Pattern Matching
java25               →  Scoped Values · Flexible Constructors · Compact Source
```

---

## Stack Tecnológica

```
Linguagem  : Java 11 (collections–jdbc) · Java 21 (orm, java21) · Java 25 (java25)
Build      : IntelliJ IDEA (.iml) · Maven (projeto02_use_cases, orm)
ORM        : Hibernate 6.4 (Jakarta EE) · JDBI 3.45
Pool       : HikariCP 5.1
Banco      : PostgreSQL 14+
Testes     : JUnit 4 (testes/) · JUnit 5 / Jupiter (orm/)
CI/CD      : GitHub Actions (projeto02_use_cases)
```

---

## Pré-requisitos

```bash
java -version   # Java 25+ para java25; Java 21+ para orm/java21; Java 11+ para demais
mvn -version    # 3.8+ (apenas projeto02_use_cases e orm)
psql --version  # PostgreSQL 14+ (apenas jdbc e orm)
```

### Banco de dados

```sql
CREATE DATABASE vendas_online;
-- Usuário: postgres / Senha: admin (configurável em persistence.xml e JdbiConfig)
```

---

## Como executar

### Módulos IntelliJ (sem pom.xml)

Abra a pasta do módulo no IntelliJ IDEA como projeto e execute a classe `Main.java`.

### Módulos Maven

```bash
cd projeto02_use_cases && mvn test
cd orm                 && mvn test
```

### Demos Java 21 / 25

```bash
cd java21/src
javac --release 21 br/com/murilo/virtualthreads/VirtualThreadsDemo.java
java -cp . br.com.murilo.virtualthreads.VirtualThreadsDemo

cd java25/src
javac --release 25 br/com/murilo/scoped/ScopedValuesDemo.java
java -cp . br.com.murilo.scoped.ScopedValuesDemo
java CompactSourceDemo.java
```

---

## Guia de Engenharia (referência transversal)

| Documento | Quando consultar |
|---|---|
| [Big O — Collections Java](./guia_engenharia/COMPLEXIDADE_ALGORITMOS.md) | Ao escolher estrutura de dados ou otimizar buscas |
| [Carga Cognitiva](./guia_engenharia/CARGA_COGNITIVA.md) | Ao revisar nomes, aninhamento, parâmetros |
| [Coesão e Acoplamento](./guia_engenharia/COESAO_ACOPLAMENTO.md) | Ao projetar classes, pacotes, dependências |
