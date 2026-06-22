# Complexidade de Algoritmos — Notação Big O

> **Objetivo:** entender o custo computacional de operações comuns em Java, usando
> as próprias estruturas de dados do projeto como referência prática.

---

## Índice

- [O que é Big O?](#o-que-é-big-o)
- [Referência visual das complexidades](#referência-visual-das-complexidades)
- [O(1) — Tempo Constante](#o1--tempo-constante)
- [O(log n) — Tempo Logarítmico](#olog-n--tempo-logarítmico)
- [O(n) — Tempo Linear](#on--tempo-linear)
- [O(n log n) — Linearítmico](#on-log-n--linearítmico)
- [O(n²) — Tempo Quadrático](#on²--tempo-quadrático)
- [Complexidade de Espaço](#complexidade-de-espaço)
- [Operações das Collections Java](#operações-das-collections-java)
- [Aplicação no projeto](#aplicação-no-projeto)

---

## O que é Big O?

Big O descreve como o **tempo de execução** ou **uso de memória** de um algoritmo
cresce em função do tamanho da entrada `n`, no **pior caso**.

```
f(n) = O(g(n))  ↔  existe c, n₀ tal que f(n) ≤ c·g(n) para todo n ≥ n₀
```

Na prática: ignora constantes e termos menores — interessa o **comportamento assintótico**.

```
O(3n + 5) → O(n)      (constante e coeficiente descartados)
O(n² + n) → O(n²)     (termo dominante prevalece)
```

---

## Referência visual das complexidades

```
Operações
     │
10⁸  │                                              O(n²)
     │                                         ···
10⁶  │                               ··········
     │                       ·········          O(n log n)
10⁴  │               ········
     │         ·······                    O(n)
10²  │  ·······
     │··················                  O(log n)
10⁰  │··················                  O(1)
     └────────────────────────────────────────────→ n
       1   10  100  1k   10k  100k  1M
```

| Complexidade | n=10 | n=100 | n=10.000 | n=1.000.000 |
|---|---|---|---|---|
| O(1)       | 1     | 1     | 1        | 1            |
| O(log n)   | 3     | 7     | 13       | 20           |
| O(n)       | 10    | 100   | 10.000   | 1.000.000    |
| O(n log n) | 33    | 664   | 132.877  | 19.931.568   |
| O(n²)      | 100   | 10.000| 100.000.000 | 10¹²      |

---

## O(1) — Tempo Constante

**Independente do tamanho da entrada.**  
O número de operações não muda quando `n` cresce.

### Exemplos em Java

```java
// ArrayList — acesso por índice (array interno)
var list = new ArrayList<>(List.of("a", "b", "c", ...));
list.get(0);            // O(1) — acessa posição de memória diretamente
list.get(list.size()-1);// O(1)
list.getFirst();        // O(1) — Java 21 SequencedCollection

// HashMap — get/put (hash table)
var map = new HashMap<String, Customer>();
map.get("cpf123");      // O(1) amortizado — calcula hash, acessa bucket
map.put("cpf123", c);   // O(1) amortizado

// ArrayDeque — push/pop nas extremidades
var deque = new ArrayDeque<Integer>();
deque.push(1);          // O(1)
deque.pop();            // O(1)
deque.peekFirst();      // O(1)
deque.peekLast();       // O(1) — Java 21 SequencedCollection

// HashSet — contains
var set = new HashSet<String>();
set.contains("java");   // O(1) amortizado
```

### No projeto — CustomerRepository (mod25_projeto02)

```java
// ANTES (O(n)) — stream percorre toda a lista
Optional<Customer> find(String cpf) {
    return customers.stream()
            .filter(c -> c.cpf().equals(cpf))
            .findFirst();
}

// DEPOIS (O(1)) — HashMap mapeia CPF → Customer diretamente
private final Map<String, Customer> customers = new HashMap<>();

Optional<Customer> find(String cpf) {
    return Optional.ofNullable(customers.get(cpf));  // O(1)
}
```

> Esta otimização é crítica: em 1 milhão de clientes, O(n) faz 1.000.000 comparações;
> O(1) faz **1** (o hash).

---

## O(log n) — Tempo Logarítmico

**Divide o problema pela metade a cada passo.**  
Muito eficiente — 1 bilhão de elementos requer apenas ~30 comparações.

### Exemplos em Java

```java
// TreeMap — get/put/containsKey (Red-Black Tree)
var treeMap = new TreeMap<String, Integer>();
treeMap.get("chave");        // O(log n) — percorre árvore balanceada
treeMap.put("chave", 1);     // O(log n)

// TreeSet — contains/add
var treeSet = new TreeSet<Integer>();
treeSet.contains(42);        // O(log n)
treeSet.first();             // O(log n)

// PriorityQueue — offer/poll (heap)
var pq = new PriorityQueue<Integer>();
pq.offer(5);                 // O(log n) — heapify up
pq.poll();                   // O(log n) — heapify down

// Collections.binarySearch — array/List ordenada
var sorted = List.of(1, 3, 5, 7, 9, 11);
Collections.binarySearch(sorted, 7);  // O(log n)
```

### Quando TreeMap vs HashMap

```java
// HashMap:  O(1) get, put — sem ordenação
// TreeMap:  O(log n) get, put — mantém chaves ordenadas

// Use TreeMap quando precisar de:
TreeMap<String, Integer> tm = new TreeMap<>();
tm.headMap("m");           // submap de tudo antes de "m"
tm.tailMap("m");           // submap de "m" em diante
tm.subMap("a", "z");       // submap entre duas chaves
tm.firstKey();             // menor chave
tm.floorKey("jose");       // maior chave ≤ "jose"
```

---

## O(n) — Tempo Linear

**Uma passagem pela entrada inteira.**

### Exemplos em Java

```java
// ArrayList.contains — busca linear
var list = new ArrayList<>(List.of("a", "b", "c", ...));
list.contains("z");          // O(n) — verifica cada elemento

// LinkedList.get(index) — percorre nós
var ll = new LinkedList<>(List.of(1, 2, 3, ...));
ll.get(500);                 // O(n) — precisa percorrer 500 nós

// Stream.filter — visita cada elemento
list.stream()
    .filter(s -> s.startsWith("j"))  // O(n)
    .toList();

// Copiar coleção
new ArrayList<>(existing);   // O(n)
List.copyOf(existing);       // O(n)

// Collections.max/min
Collections.max(list);       // O(n) — lê todos
```

### Armadilha: O(n) dentro de O(n) = O(n²)

```java
// O(n²) — para cada customer, busca na lista de orders
for (Customer c : customers) {              // O(n)
    for (Order o : orders) {               // O(n)
        if (o.customerId().equals(c.id())) // O(1)
            process(c, o);
    }
}

// O(n) — pré-indexar orders por customerId
Map<Long, List<Order>> ordersByCustomer = orders.stream()
    .collect(Collectors.groupingBy(Order::customerId));  // O(n) uma vez

for (Customer c : customers) {                           // O(n)
    var customerOrders = ordersByCustomer.getOrDefault(c.id(), List.of()); // O(1)
    process(c, customerOrders);
}
```

---

## O(n log n) — Linearítmico

**Ótimo teórico para algoritmos de ordenação baseados em comparação.**

### Exemplos em Java

```java
// Collections.sort — TimSort
Collections.sort(list);          // O(n log n)
list.sort(Comparator.naturalOrder()); // O(n log n)

// Arrays.sort para objetos — TimSort
Arrays.sort(objArray);           // O(n log n)

// Stream.sorted
list.stream().sorted().toList(); // O(n log n)

// TreeSet construído de uma coleção
new TreeSet<>(list);             // O(n log n)
```

> **Arrays.sort para primitivos** usa Dual-Pivot QuickSort — O(n log n) médio, O(n²) pior caso
> (mas extremamente raro em prática).

---

## O(n²) — Tempo Quadrático

**Geralmente inaceitável para n > 10.000.**

### Exemplos em Java

```java
// Bubble sort manual
for (int i = 0; i < n; i++)
    for (int j = 0; j < n - i - 1; j++)
        if (arr[j] > arr[j+1]) swap(arr, j, j+1);  // O(n²)

// Verificar duplicatas (ingênuo)
for (int i = 0; i < list.size(); i++)
    for (int j = i+1; j < list.size(); j++)
        if (list.get(i).equals(list.get(j))) duplicates.add(list.get(i));

// Solução O(n) com Set:
var seen = new HashSet<>();
var duplicates = list.stream()
    .filter(e -> !seen.add(e))   // add retorna false se já existia
    .toList();                   // O(n) total
```

---

## Complexidade de Espaço

Big O também mede **memória utilizada**:

| Estrutura | Espaço |
|---|---|
| Variável primitiva | O(1) |
| Array de n elementos | O(n) |
| HashMap com n entradas | O(n) |
| Recursão profundidade d | O(d) — stack frames |
| Merge sort | O(n) — array auxiliar |
| Quick sort in-place | O(log n) — stack de recursão |

---

## Operações das Collections Java

### List

| Operação | ArrayList | LinkedList |
|---|---|---|
| `get(index)` | **O(1)** | O(n) |
| `add(end)` | O(1) amortizado | **O(1)** |
| `add(index)` | O(n) | O(n) |
| `remove(index)` | O(n) | O(n) |
| `contains` | O(n) | O(n) |
| `size()` | **O(1)** | **O(1)** |
| `getFirst()` (Java 21) | **O(1)** | **O(1)** |
| `getLast()` (Java 21) | **O(1)** | **O(1)** |

> **Regra prática:** prefira `ArrayList` para acesso aleatório; `LinkedList` não tem vantagem
> em quase nenhum caso real — `ArrayDeque` é superior para fila/pilha.

### Map

| Operação | HashMap | LinkedHashMap | TreeMap |
|---|---|---|---|
| `get/put` | **O(1)** amort. | **O(1)** amort. | O(log n) |
| `containsKey` | **O(1)** amort. | **O(1)** amort. | O(log n) |
| `remove` | **O(1)** amort. | **O(1)** amort. | O(log n) |
| Iteração ordenada | ❌ | por inserção | por chave |
| `firstKey/lastKey` | ❌ | `firstEntry()` (Java 21) | **O(log n)** |

### Set

| Operação | HashSet | LinkedHashSet | TreeSet |
|---|---|---|---|
| `add/contains/remove` | **O(1)** amort. | **O(1)** amort. | O(log n) |
| Iteração | sem ordem | por inserção | ordenada |

### Queue / Deque

| Operação | ArrayDeque | PriorityQueue | LinkedList |
|---|---|---|---|
| `offer/peek/poll` | **O(1)** amort. | O(log n) | **O(1)** |
| `get(index)` | ❌ | ❌ | O(n) |
| Acesso a mínimo/máximo | ❌ | **O(1)** para mín | ❌ |

---

## Aplicação no projeto

### mod11 — Collections

```java
// Comparar: BFS com LinkedList (fila) = O(V + E)
Queue<Node> fila = new ArrayDeque<>(); // O(1) offer/poll — melhor que LinkedList
fila.offer(raiz);
while (!fila.isEmpty()) {
    var node = fila.poll();    // O(1)
    for (var filho : node.filhos()) fila.offer(filho); // O(1) por filho
}
```

### mod12 — HashMap vs TreeMap

```java
// Contagem de frequência — HashMap O(n)
Map<String, Long> freq = palavras.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

// Top-N — TreeMap O(n log n) se precisar de ordenação
new TreeMap<>(freq).descendingMap().entrySet().stream().limit(10)...
```

### mod17 — Generic Repository

```java
// Interface genérica — complexidade depende da implementação
interface Repository<T, ID> {
    Optional<T> findById(ID id);  // O(1) com HashMap, O(log n) com TreeMap
    List<T> findAll();            // O(n) — retorna todos
    void save(T entity);          // O(1) com HashMap
}
```

### mod25_projeto02 — CustomerRepository

```java
// Map<String, Customer> — garante O(1) para find/delete/save
// Antes: List + stream().filter() = O(n)
// Depois: HashMap.get(cpf)        = O(1)
```

---

## Resumo rápido

```
O(1)      → HashMap.get, ArrayList.get(i), ArrayDeque.push
O(log n)  → TreeMap.get, PriorityQueue.poll, binarySearch
O(n)      → List.contains, stream.filter, Collections.max
O(n log n)→ Collections.sort, Stream.sorted, new TreeSet(list)
O(n²)     → loops aninhados sem pré-indexação
```

**Regra de ouro:** quando precisar de busca por chave repetida, use `HashMap` (O(1)).  
Quando precisar de ordenação natural, use `TreeMap`/`TreeSet` (O(log n)).  
Nunca faça busca linear dentro de outro loop — pre-indexe com um `Map`.
