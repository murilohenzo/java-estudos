# Mod 29 — JDBC: Acesso Direto ao Banco de Dados

> **Conceito central:** Conexão Java com banco relacional sem ORM — JDBC puro.  
> `DriverManager` · `Connection` · `PreparedStatement` · `ResultSet` · `ConnectionFactory` · DAO Pattern

---

## Conteúdo

```
mod29/src/main/java/br/com/murilo/
├── domain/
│   └── Client.java                        # Entidade com id, name, cpf, address, phone
└── dao/
    └── generic/
        └── jdbc/
            ├── ConnectionFactory.java     # Singleton de conexão JDBC
            └── dao/
                └── ClientDAOImpl.java     # DAO JDBC (stubs — base para implementação)
```

---

## JDBC — Camadas da Stack de Persistência

```
Código Java (DAO)
       │
  java.sql.* (JDBC API)
       │
  Driver JDBC (org.postgresql:postgresql)
       │
  PostgreSQL Server
```

JDBC é a camada mais baixa de acesso a banco em Java. JPA/Hibernate e JDBI constroem sobre JDBC.

---

## ConnectionFactory — Singleton de conexão

```java
public class ConnectionFactory {
    private static ConnectionFactory instance;
    private Connection connection;

    private static final String URL  = "jdbc:postgresql://localhost:5432/vendas_online";
    private static final String USER = "postgres";
    private static final String PASS = "admin";

    private ConnectionFactory() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco de dados", e);
        }
    }

    public static ConnectionFactory getInstance() {
        if (instance == null) instance = new ConnectionFactory();
        return instance;
    }

    public Connection getConnection() { return connection; }
}
```

**Limitação:** `DriverManager.getConnection()` abre uma conexão TCP a cada chamada.
Em produção, use **connection pool** (HikariCP, c3p0, DBCP) — veja [mod31](../mod31/README.md).

---

## ClientDAOImpl — Operações JDBC

```java
public class ClientDAOImpl {
    private final Connection conn = ConnectionFactory.getInstance().getConnection();

    public Client save(Client client) throws SQLException {
        String sql = "INSERT INTO TB_CLIENT (name, cpf, address, phone) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getCpf());
            ps.setString(3, client.getAddress());
            ps.setString(4, client.getPhone());
            ps.executeUpdate();

            // Recupera o ID gerado pelo banco
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) client.setId(rs.getLong(1));
            }
        }
        return client;
    }

    public Optional<Client> findByCpf(String cpf) throws SQLException {
        String sql = "SELECT id, name, cpf, address, phone FROM TB_CLIENT WHERE cpf = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToClient(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Client> findAll() throws SQLException {
        String sql = "SELECT id, name, cpf, address, phone FROM TB_CLIENT ORDER BY name";
        List<Client> clients = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clients.add(mapRowToClient(rs));
            }
        }
        return clients;
    }

    public void delete(Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM TB_CLIENT WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Client mapRowToClient(ResultSet rs) throws SQLException {
        return new Client(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("cpf"),
            rs.getString("address"),
            rs.getString("phone")
        );
    }
}
```

---

## JDBC — Conceitos Fundamentais

### PreparedStatement vs Statement

```java
// ❌ Statement — vulnerável a SQL Injection
String sql = "SELECT * FROM users WHERE name = '" + userInput + "'";
// userInput = "' OR '1'='1" → SELECT * FROM users WHERE name = '' OR '1'='1'

// ✅ PreparedStatement — parâmetros são sempre tratados como dados
PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
ps.setString(1, userInput);  // sempre seguro
```

### Transações JDBC

```java
Connection conn = ConnectionFactory.getInstance().getConnection();
conn.setAutoCommit(false);  // início de transação explícita

try {
    // múltiplas operações atômicas
    ps1.executeUpdate();
    ps2.executeUpdate();

    conn.commit();  // confirma
} catch (SQLException e) {
    conn.rollback();  // desfaz tudo em caso de erro
    throw e;
} finally {
    conn.setAutoCommit(true);  // restaura modo padrão
}
```

### Try-with-resources

```java
// Connection, PreparedStatement e ResultSet implementam AutoCloseable
try (Connection conn   = DriverManager.getConnection(url, user, pass);
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs      = ps.executeQuery()) {

    while (rs.next()) { /* processa */ }

} // conn, ps, rs fechados automaticamente — mesmo em exceção
```

---

## Comparativo: JDBC vs JDBI vs JPA

| Critério | JDBC puro | JDBI | JPA/Hibernate |
|----------|-----------|------|---------------|
| Nível de abstração | Baixo | Médio | Alto |
| SQL | Explícito | Explícito | Gerado |
| Boilerplate | Alto | Médio | Baixo |
| Performance | Máxima | Alta | Boa |
| Curva de aprendizado | Baixa | Baixa | Alta |
| Mapeamento OR/M | Manual | Semi-automático | Automático |
| Uso recomendado | Aprendizado, performance crítica | Read models, queries complexas | Domain models ricos |

---

## DDL — Setup do banco

```sql
CREATE TABLE TB_CLIENT (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    cpf     VARCHAR(14)  NOT NULL UNIQUE,
    address TEXT,
    phone   VARCHAR(20)
);

-- Conectar com:
-- Host: localhost:5432
-- DB: vendas_online
-- User: postgres / Pass: admin
```

---

## Próximo Módulo

[mod31 — ORM: JPA / Hibernate / JDBI com JDK 21](../mod31/README.md)
