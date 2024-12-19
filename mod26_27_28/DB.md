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

### **2.1 Funções de Agregação**

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

### **2.2 Funções de Manipulação de Strings**

| **Função**         | **Descrição**                              | **Exemplo**                                                  | **Resultado**                              |
|---------------------|--------------------------------------------|--------------------------------------------------------------|--------------------------------------------|
| `UPPER()`           | Converte texto para maiúsculas.           | `SELECT UPPER(nome) FROM clientes;`                         | `"JOÃO"`                                   |
| `LOWER()`           | Converte texto para minúsculas.           | `SELECT LOWER(nome) FROM clientes;`                         | `"joão"`                                   |
| `SUBSTR()`          | Retorna parte de uma string.              | `SELECT SUBSTR(nome, 1, 3) FROM clientes;`                  | `"Joã"`                                   |
| `LENGTH()`          | Retorna o tamanho da string.              | `SELECT LENGTH(nome) FROM clientes;`                        | `4`                                        |
| `TRIM()`            | Remove espaços em branco.                 | `SELECT TRIM(' João ') FROM clientes;`                      | `"João"`                                   |
| `REPLACE()`         | Substitui parte da string por outra.       | `SELECT REPLACE(nome, 'João', 'José') FROM clientes;`       | `"José"`                                   |

**Exemplos Práticos**

- **Formatando Nomes:**
  ```sql
  SELECT UPPER(nome) AS nome_maiusculo
  FROM clientes;
  ```

- **Extraindo Parte de um Texto:**
  ```sql
  SELECT SUBSTR(descricao, 1, 50) AS resumo
  FROM artigos;
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

## **3. Cláusula `HAVING`**

A cláusula `HAVING` é utilizada para filtrar os resultados de consultas agrupadas (`GROUP BY`). Ela é aplicada **após o agrupamento** e permite o uso de funções de agregação.

### **Sintaxe do `HAVING`**

```sql
SELECT coluna1, coluna2, AGREGACAO(funcao_coluna)
FROM tabela
GROUP BY coluna1, coluna2
HAVING condicao_agregada;
```

### **Diferença entre `WHERE` e `HAVING`**

| **Aspecto**        | **`WHERE`**                                               | **`HAVING`**                                               |
|---------------------|----------------------------------------------------------|-----------------------------------------------------------|
| **Quando é usado**  | Antes do agrupamento.                                     | Depois do agrupamento.                                     |
| **Base da condição**| Colunas individuais ou expressões.                        | Funções de agregação ou colunas agregadas.                 |
| **Exemplo**         | `WHERE idade > 18`                                       | `HAVING AVG(salario) > 5000`                              |

**Exemplos Práticos**

- **Filtrando Grupos com Média Maior que um Valor:**
  ```sql
  SELECT departamento, AVG(salario) AS media_salarial
  FROM funcionarios
  GROUP BY departamento
  HAVING AVG(salario) > 5000;
  ```

- **Contando e Filtrando Grupos:**
  ```sql
  SELECT cidade, COUNT(*) AS total_clientes
  FROM clientes
  GROUP BY cidade
  HAVING COUNT(*) > 10;
  ```

- **Combinação de `WHERE` e `HAVING`:**
  ```sql
  SELECT curso, COUNT(*) AS total_alunos
  FROM alunos
  WHERE idade > 18
  GROUP BY curso
  HAVING COUNT(*) >= 5;
  ```

---

## **4. Cláusula `CASE WHEN`**

A cláusula `CASE WHEN` permite criar condições personalizadas em uma consulta SQL, funcionando como uma instrução condicional.

### **Sintaxe do `CASE WHEN`**

```sql
SELECT coluna,
       CASE
           WHEN condicao1 THEN resultado1
           WHEN condicao2 THEN resultado2
           ELSE resultado_padrao
       END AS alias
FROM tabela;
```

**Exemplos Práticos**

- **Classificando Faixa Etária:**
  ```sql
  SELECT nome,
         CASE
             WHEN idade < 18 THEN 'Menor de Idade'
             WHEN idade BETWEEN 18 AND 59 THEN 'Adulto'
             ELSE 'Idoso'
         END AS faixa_etaria
  FROM clientes;
  ```

- **Atribuindo Desconto por Categoria:**
  ```sql
  SELECT produto,
         CASE
             WHEN categoria = 'eletrônicos' THEN preco * 0.9
             WHEN categoria = 'livros' THEN preco * 0.85
             ELSE preco
         END AS preco_com_desconto
  FROM produtos;
  ```


