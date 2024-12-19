# Documentação Completa de SQL

## Introdução ao SQL
SQL (*Structured Query Language*) é a linguagem padrão para gerenciamento de bancos de dados relacionais. Ela permite criar, consultar, atualizar e excluir dados de tabelas.

---

## **1. Operadores em SQL**

### **1.1 Operadores Lógicos**

| **Operador** | **Descrição**                               | **Exemplo**                                                |
|---------------|---------------------------------------------|------------------------------------------------------------|
| `AND`         | Retorna `TRUE` se todas as condições forem verdadeiras. | `SELECT * FROM alunos WHERE curso = 'TI' AND idade > 18;` |
| `OR`          | Retorna `TRUE` se pelo menos uma condição for verdadeira. | `SELECT * FROM alunos WHERE curso = 'TI' OR curso = 'ADM';` |
| `NOT`         | Inverte o valor lógico de uma condição.    | `SELECT * FROM alunos WHERE NOT cidade = 'SP';`           |
| `IN`          | Verifica se um valor está em uma lista de valores. | `SELECT * FROM produtos WHERE categoria IN ('eletrônicos', 'livros');` |
| `BETWEEN`     | Verifica se um valor está dentro de um intervalo. | `SELECT * FROM vendas WHERE data BETWEEN '2024-01-01' AND '2024-12-31';` |
| `LIKE`        | Busca valores com um padrão específico.    | `SELECT * FROM clientes WHERE nome LIKE 'João%';`           |
| `IS NULL`     | Verifica se o valor é nulo.               | `SELECT * FROM pedidos WHERE data_entrega IS NULL;`         |
| `EXISTS`      | Verifica se uma subconsulta retorna resultados. | `SELECT * FROM alunos WHERE EXISTS (SELECT * FROM notas WHERE notas.aluno_id = alunos.id);` |

**Exemplos Práticos**

- **Usando `AND` e `OR`:**
  ```sql
  SELECT * FROM alunos
  WHERE curso = 'TI' AND idade > 18 OR cidade = 'SP';
  ```

- **Usando `IN` e `BETWEEN`:**
  ```sql
  SELECT * FROM vendas
  WHERE categoria IN ('eletrônicos', 'livros')
  AND data BETWEEN '2024-01-01' AND '2024-12-31';
  ```

---

### **1.2 Operadores Aritméticos**

| **Operador** | **Descrição**         | **Exemplo**                                 | **Resultado** |
|--------------|------------------------|---------------------------------------------|---------------|
| `+`          | Soma                  | `SELECT preco + 10 FROM produtos;`          | Adiciona 10 ao preço. |
| `-`          | Subtração             | `SELECT quantidade - 5 FROM estoque;`       | Subtrai 5 da quantidade. |
| `*`          | Multiplicação         | `SELECT quantidade * preco FROM vendas;`    | Calcula o total da venda. |
| `/`          | Divisão               | `SELECT preco / 2 FROM produtos;`           | Divide o preço por 2. |
| `%`          | Módulo (resto da divisão) | `SELECT quantidade % 3 FROM estoque;`    | Retorna o resto da divisão por 3. |

**Exemplos Práticos**

- **Calculando o Total de Vendas:**
  ```sql
  SELECT produto, quantidade * preco AS total_venda
  FROM vendas;
  ```

- **Verificando Estoque Múltiplo de 5:**
  ```sql
  SELECT produto
  FROM estoque
  WHERE quantidade % 5 = 0;
  ```

---

## **2. Funções em SQL**

### **2.1 Funções Agregadas**

Funções agregadas realizam operações em um conjunto de valores e retornam um único valor resumido.

| **Função**     | **Descrição**                       | **Exemplo**                                                | **Resultado** |
|-----------------|-------------------------------------|------------------------------------------------------------|---------------|
| `COUNT()`       | Conta o número de linhas.          | `SELECT COUNT(*) FROM alunos;`                             | Número total de alunos. |
| `SUM()`         | Soma os valores de uma coluna.     | `SELECT SUM(salario) FROM funcionarios;`                  | Soma total dos salários. |
| `AVG()`         | Calcula a média dos valores.       | `SELECT AVG(nota) FROM notas;`                             | Média das notas. |
| `MAX()`         | Retorna o maior valor.             | `SELECT MAX(preco) FROM produtos;`                         | Maior preço. |
| `MIN()`         | Retorna o menor valor.             | `SELECT MIN(idade) FROM alunos;`                           | Menor idade. |

**Exemplos Práticos**

- **Calculando a Média Salarial por Departamento:**
  ```sql
  SELECT departamento, AVG(salario) AS media_salarial
  FROM funcionarios
  GROUP BY departamento;
  ```

- **Verificando Estoque Total por Categoria:**
  ```sql
  SELECT categoria, SUM(quantidade) AS total_estoque
  FROM produtos
  GROUP BY categoria;
  ```

---

### **2.2 Funções Escalares**

Funções escalares realizam operações em cada linha individualmente e retornam um valor para cada linha.

| **Função**         | **Descrição**                              | **Exemplo**                                                  | **Resultado**                              |
|---------------------|--------------------------------------------|--------------------------------------------------------------|--------------------------------------------|
| `UPPER()`           | Converte texto para maiúsculas.           | `SELECT UPPER(nome) FROM clientes;`                         | `"JOÃO"`                                   |
| `LOWER()`           | Converte texto para minúsculas.           | `SELECT LOWER(nome) FROM clientes;`                         | `"joão"`                                   |
| `SUBSTR()`          | Retorna parte de uma string.              | `SELECT SUBSTR(nome, 1, 3) FROM clientes;`                  | `"Joã"`                                   |
| `LENGTH()`          | Retorna o tamanho da string.              | `SELECT LENGTH(nome) FROM clientes;`                        | `4`                                        |
| `TRIM()`            | Remove espaços em branco.                 | `SELECT TRIM(' João ') FROM clientes;`                      | `"João"`                                   |
| `REPLACE()`         | Substitui parte da string por outra.       | `SELECT REPLACE(nome, 'João', 'José') FROM clientes;`       | `"José"`                                   |
| `ROUND()`           | Arredonda um número para um determinado número de casas decimais. | `SELECT ROUND(salario, 2) FROM funcionarios;` | `1234.57` |
| `ABS()`             | Retorna o valor absoluto de um número.   | `SELECT ABS(-10) AS positivo;`                              | `10`                                       |

**Exemplos Práticos**

- **Formatando Nomes:**
  ```sql
  SELECT UPPER(nome) AS nome_maiusculo
  FROM clientes;
  ```

- **Arredondando Valores:**
  ```sql
  SELECT ROUND(preco, 1) AS preco_arredondado
  FROM produtos;
  ```

---

### **2.3 Funções de Manipulação de Datas**

| **Função**           | **Descrição**                                 | **Exemplo**                                                                 | **Resultado**                         |
|-----------------------|-----------------------------------------------|-----------------------------------------------------------------------------|---------------------------------------|
| `NOW()`              | Retorna a data e hora atuais.                | `SELECT NOW();`                                                            | `2024-12-19 14:35:00`                |
| `DATE()`             | Retorna apenas a data.                       | `SELECT DATE('2024-12-25 14:35:00');`                                      | `2024-12-25`                         |
| `STRFTIME()`         | Formata uma data para um formato específico. | `SELECT STRFTIME('%d/%m/%Y', '2024-12-25');`                               | `25/12/2024`                         |
| `YEAR()`             | Retorna o ano de uma data.                   | `SELECT STRFTIME('%Y', '2024-12-25');`                                     | `2024`                                |
| `MONTH()`            | Retorna o mês de uma data.                   | `SELECT STRFTIME('%m', '2024-12-25');`                                     | `12`                                  |
| `DAY()`              | Retorna o dia de uma data.                   | `SELECT STRFTIME('%d', '2024-12-25');`                                     | `25`                                  |
| `DATEDIFF()`         | Calcula a diferença entre duas datas.        | `SELECT JULIANDAY('2024-12-25') - JULIANDAY('2024-12-20');`                | `5`                                   |

**Exemplos Práticos**

- **Formatando Datas:**
  ```sql
  SELECT STRFTIME('%d/%m/%Y', data_pedido) AS data_formatada
  FROM pedidos;
  ```

- **Calculando Dias Restantes para Entrega:**
  ```sql
  SELECT pedido_id, JULIANDAY(data_entrega) - JULIANDAY(NOW()) AS dias_restantes
  FROM pedidos;
  ```
---

## **3. Joins em SQL**

### **3.1 Tipos de Joins**

| **Tipo de Join**    | **Descrição**                                                                 | **Exemplo**                                                                 |
|---------------------|---------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| `INNER JOIN`        | Retorna registros que têm correspondência em ambas as tabelas.                     | `SELECT * FROM alunos INNER JOIN cursos ON alunos.curso_id = cursos.id;`   |
| `LEFT JOIN`         | Retorna todos os registros da tabela à esquerda e os correspondentes da direita.   | `SELECT * FROM alunos LEFT JOIN notas ON alunos.id = notas.aluno_id;`      |
| `RIGHT JOIN`        | Retorna todos os registros da tabela à direita e os correspondentes da esquerda.   | `SELECT * FROM notas RIGHT JOIN alunos ON notas.aluno_id = alunos.id;`     |
| `FULL OUTER JOIN`   | Retorna todos os registros quando há correspondência em qualquer tabela.            | `SELECT * FROM alunos FULL OUTER JOIN cursos ON alunos.curso_id = cursos.id;` |

**Exemplo Prático de Joins**

- **Consulta de Alunos e Notas:**
  ```sql
  SELECT alunos.nome, notas.nota
  FROM alunos
  INNER JOIN notas ON alunos.id = notas.aluno_id;
  ```

---

## **4. Subqueries em SQL**

Subqueries são consultas aninhadas dentro de outras consultas.

### **Tipos de Subqueries**

1. **Subqueries em `SELECT`:**
   ```sql
   SELECT nome, (SELECT AVG(nota) FROM notas WHERE notas.aluno_id = alunos.id) AS media_notas
   FROM alunos;
   ```

2. **Subqueries em `WHERE`:**
   ```sql
   SELECT nome
   FROM alunos
   WHERE id IN (SELECT aluno_id FROM notas WHERE nota > 7);
   ```

3. **Subqueries em `FROM`:**
   ```sql
   SELECT AVG(media_notas) AS media_geral
   FROM (SELECT AVG(nota) AS media_notas FROM notas GROUP BY aluno_id);
   ```

---

## **5. Triggers em SQL**

Triggers são comandos automáticos executados em resposta a eventos no banco de dados.

### **Criando um Trigger**

```sql
CREATE TRIGGER atualiza_estoque
AFTER INSERT ON vendas
FOR EACH ROW
BEGIN
  UPDATE produtos
  SET quantidade = quantidade - NEW.quantidade
  WHERE id = NEW.produto_id;
END;
```

---

## **6. Views em SQL**

Views são consultas salvas como objetos no banco de dados.

### **Criando uma View**

```sql
CREATE VIEW vendas_resumo AS
SELECT produto_id, SUM(quantidade) AS total_vendido
FROM vendas
GROUP BY produto_id;
```

### **Usando uma View**

```sql
SELECT * FROM vendas_resumo;
```

---

## **7. Indexação em SQL**

### **O que são índices?**

Índices são estruturas que otimizam o desempenho das consultas, permitindo acesso mais rápido aos dados.

### **Criando um índice**

- **Índice Simples:**
  ```sql
  CREATE INDEX idx_nome ON clientes (nome);
  ```

- **Índice Composto:**
  ```sql
  CREATE INDEX idx_nome_cidade ON clientes (nome, cidade);
  ```

### **Usando Índices**

Os índices são usados automaticamente pelo banco de dados em consultas como:
```sql
SELECT * FROM clientes WHERE nome = 'João';
```

### **Boas Práticas de índices**

1. Crie índices em colunas frequentemente usadas em filtros (`WHERE`) e joins.
2. Evite índices em colunas com muitos valores repetidos (baixa cardinalidade).
3. Use índices compostos para consultas que filtram por mais de uma coluna.

---

## **8. UNION e UNION ALL**

### **Diferença entre UNION e UNION ALL**

| **Operador**   | **Descrição**                                                                 |
|----------------|-------------------------------------------------------------------------------|
| `UNION`       | Combina os resultados de duas ou mais consultas, removendo duplicatas.        |
| `UNION ALL`   | Combina os resultados de duas ou mais consultas, mantendo duplicatas.         |

### **Exemplo com UNION**

```sql
SELECT nome, cidade FROM clientes_a
UNION
SELECT nome, cidade FROM clientes_b;
```

- **Resultado:** Retorna os nomes e cidades de ambas as tabelas, sem duplicatas.

### **Exemplo com UNION ALL**

```sql
SELECT nome, cidade FROM clientes_a
UNION ALL
SELECT nome, cidade FROM clientes_b;
```

- **Resultado:** Retorna os nomes e cidades de ambas as tabelas, incluindo duplicatas.

### **Regras para UNION e UNION ALL**

1. O mesmo número de colunas deve ser retornado em todas as consultas.
2. As colunas devem ter tipos de dados compatíveis.

---

## **9. Boas Práticas para Performance em SQL**

1. **Utilize índices**:
   - Crie índices em colunas frequentemente usadas em `WHERE`, `JOIN` e `ORDER BY`.

2. **Evite `SELECT *`**:
   - Selecione apenas as colunas necessárias para reduzir o consumo de recursos.

3. **Normalize os Dados**:
   - Reduza a redundância, mas mantenha um equilíbrio para evitar joins excessivos.

4. **Use Particionamento**:
   - Divida tabelas grandes em partes menores para melhorar o desempenho.

5. **Analise os Planos de Execução**:
   - Use comandos como `EXPLAIN` para identificar gargalos em consultas.

---

## **10. Locks em Banco de Dados**

Locks são usados para garantir consistência e integridade dos dados, mas podem causar problemas de desempenho se mal gerenciados.

### **Tipos de Locks**

| **Tipo**              | **Descrição**                                                                     |
|-----------------------|---------------------------------------------------------------------------------|
| **Shared Lock (S)**   | Permite leitura simultânea, mas bloqueia escrita.                                |
| **Exclusive Lock (X)**| Bloqueia leitura e escrita para outras transações.                              |
| **Deadlock**          | Ocorre quando duas ou mais transações se bloqueiam mutuamente.                  |

### **Evitar Problemas de Locks**

1. **Ordene as Operações:**
   - Sempre acesse os recursos na mesma ordem em transações.

2. **Use Transações Curta:**
   - Limite a quantidade de operações dentro de uma transação.

3. **Habilite Timeout:**
   - Configure um tempo limite para detecção de deadlocks.

---
