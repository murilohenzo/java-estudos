# Mod 26/27/28 — SQL: Fundamentos, Avançado e Otimização

> **Referência completa de SQL** para uso com JDBC (mod29) e JPA (mod31).  
> DDL · DML · Operadores · Joins · Subqueries · Triggers · Views · Índices · Locks

> A documentação completa está em [DB.md](./DB.md).

---

## Conteúdo

```
mod26_27_28/
└── DB.md    # Documentação SQL completa com exemplos práticos
```

---

## Mapa de Conceitos SQL

### DDL — Data Definition Language

```sql
-- Criar tabela com constraints
CREATE TABLE TB_COURSE (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(10)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Adicionar constraint após criação
ALTER TABLE TB_REGISTRATION
    ADD CONSTRAINT fk_course
    FOREIGN KEY (course_id) REFERENCES TB_COURSE(id) ON DELETE CASCADE;

-- Índice composto para queries frequentes
CREATE UNIQUE INDEX idx_course_code ON TB_COURSE (UPPER(code));
```

### DML — Data Manipulation Language

```sql
-- INSERT com retorno do ID gerado (PostgreSQL)
INSERT INTO TB_COURSE (code, name, description)
VALUES ('JAVA-01', 'Java Backend', 'Curso completo')
RETURNING id;

-- UPDATE com JOIN (PostgreSQL)
UPDATE TB_REGISTRATION r
SET status = 'INACTIVE'
FROM TB_COURSE c
WHERE r.course_id = c.id
  AND c.code = 'JAVA-01';

-- DELETE com subquery
DELETE FROM TB_REGISTRATION
WHERE course_id IN (
    SELECT id FROM TB_COURSE WHERE name LIKE '%TEMP%'
);
```

---

## Tipos de JOIN

```sql
-- INNER JOIN — apenas registros com correspondência em AMBAS as tabelas
SELECT c.name, r.code, r.status
FROM TB_COURSE c
INNER JOIN TB_REGISTRATION r ON c.id = r.course_id;

-- LEFT JOIN — todos os cursos, mesmo sem matrícula
SELECT c.name, COUNT(r.id) AS total_registrations
FROM TB_COURSE c
LEFT JOIN TB_REGISTRATION r ON c.id = r.course_id
GROUP BY c.name;

-- FULL OUTER JOIN — todos de ambos os lados
SELECT c.name, r.code
FROM TB_COURSE c
FULL OUTER JOIN TB_REGISTRATION r ON c.id = r.course_id;
```

| JOIN | Inclui sem correspondência do LEFT | Inclui sem correspondência do RIGHT |
|------|-----------------------------------|-------------------------------------|
| INNER | ❌ | ❌ |
| LEFT  | ✅ | ❌ |
| RIGHT | ❌ | ✅ |
| FULL  | ✅ | ✅ |

---

## Funções Agregadas

```sql
-- Estatísticas por cidade
SELECT
    city,
    COUNT(*)           AS total_customers,
    AVG(registration_value) AS avg_value,
    MAX(registration_value) AS max_value,
    MIN(registration_value) AS min_value,
    SUM(registration_value) AS total_revenue
FROM TB_CUSTOMER c
JOIN TB_REGISTRATION r ON c.id = r.customer_id
GROUP BY city
HAVING COUNT(*) > 5          -- HAVING filtra APÓS o GROUP BY
ORDER BY total_revenue DESC;
```

---

## Subqueries

```sql
-- Correlated subquery — referencia coluna da query externa
SELECT c.name
FROM TB_COURSE c
WHERE (
    SELECT COUNT(*) FROM TB_REGISTRATION r WHERE r.course_id = c.id
) > 10;

-- Subquery como tabela derivada (FROM)
SELECT city, avg_age
FROM (
    SELECT city, AVG(age) AS avg_age
    FROM TB_CUSTOMER
    GROUP BY city
) subquery
WHERE avg_age > 25;

-- EXISTS — mais eficiente que IN para grandes datasets
SELECT c.name
FROM TB_COURSE c
WHERE EXISTS (
    SELECT 1 FROM TB_REGISTRATION r
    WHERE r.course_id = c.id AND r.status = 'ACTIVE'
);
```

---

## Views e Triggers

```sql
-- View — consulta salva como objeto
CREATE VIEW vw_active_registrations AS
SELECT c.name AS course_name, r.code, r.value, r.status, r.created_at
FROM TB_COURSE c
JOIN TB_REGISTRATION r ON c.id = r.course_id
WHERE r.status = 'ACTIVE';

-- Trigger — executa automaticamente em evento DML
CREATE OR REPLACE FUNCTION update_audit_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_audit
BEFORE UPDATE ON TB_COURSE
FOR EACH ROW EXECUTE FUNCTION update_audit_timestamp();
```

---

## Índices e Performance

```sql
-- Índice simples
CREATE INDEX idx_registration_status ON TB_REGISTRATION(status);

-- Índice parcial — apenas rows que satisfazem condição (menor tamanho)
CREATE INDEX idx_active_registrations ON TB_REGISTRATION(course_id)
WHERE status = 'ACTIVE';

-- Índice funcional — para queries com funções
CREATE INDEX idx_course_code_upper ON TB_COURSE (UPPER(code));
-- Permite: WHERE UPPER(code) = UPPER(:input) usar o índice

-- Verificar plano de execução
EXPLAIN ANALYZE
SELECT * FROM TB_COURSE WHERE UPPER(code) = 'JAVA-01';
```

**Boas práticas de indexação:**
- Indexe colunas em `WHERE`, `JOIN ON`, `ORDER BY` frequentes
- Evite índices em colunas com baixa cardinalidade (ex: boolean)
- Índice composto: ordem importa — coluna mais seletiva primeiro
- Monitore índices não utilizados: `pg_stat_user_indexes`

---

## Locks e Transações

```sql
-- Transação explícita com rollback
BEGIN;
    UPDATE TB_COURSE SET name = 'Novo Nome' WHERE id = 1;
    UPDATE TB_REGISTRATION SET status = 'INACTIVE' WHERE course_id = 1;
    -- Se qualquer UPDATE falhar, ROLLBACK desfaz ambos
COMMIT;  -- ou ROLLBACK;

-- Pessimistic locking — bloqueia rows para update
SELECT * FROM TB_COURSE WHERE id = 1 FOR UPDATE;

-- Optimistic locking — usa coluna de versão
UPDATE TB_COURSE
SET name = 'Novo', version = version + 1
WHERE id = 1 AND version = 5;  -- falha se outra transação atualizou
```

---

## SQL usado no mod31 (JPA/JDBI)

As tabelas criadas por `hibernate.hbm2ddl.auto = update`:

```sql
-- TB_COURSE
CREATE TABLE TB_COURSE (
    id          BIGINT PRIMARY KEY,
    code        VARCHAR(10)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    CONSTRAINT uq_course_code UNIQUE (code)
);
CREATE INDEX idx_course_code ON TB_COURSE (code);

-- TB_REGISTRATION
CREATE TABLE TB_REGISTRATION (
    id         BIGINT PRIMARY KEY,
    code       VARCHAR(10)  NOT NULL,
    value      FLOAT8       NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    course_id  BIGINT       NOT NULL REFERENCES TB_COURSE(id),
    CONSTRAINT uq_registration_code UNIQUE (code)
);
CREATE INDEX idx_registration_course ON TB_REGISTRATION (course_id);

-- Sequences
CREATE SEQUENCE sq_course      START 1 INCREMENT 50;
CREATE SEQUENCE sq_registration START 1 INCREMENT 50;
```

---

## Próximo Módulo

[mod29 — JDBC: Acesso direto ao banco](../mod29/README.md)
