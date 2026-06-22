# Mod 11 — Collections: List e Queue

> **Conceito central:** Estruturas de dados lineares da Java Collections Framework.  
> `ArrayList` · `PriorityQueue` · `LinkedList` · `ArrayDeque` · Comparators

---

## Conteúdo

```
mod11/
├── collection_list/
│   ├── User.java             # Entidade com atributos name e age
│   ├── CompareUserAge.java   # Comparator<User> por idade
│   └── Main.java             # ArrayList, Collections.sort(), Comparator
└── collection_queue/
    ├── User.java
    ├── ExamplePriorityQueue.java  # PriorityQueue com Comparator
    ├── ExemploLinkedList.java     # LinkedList como Deque (addFirst, addLast)
    └── ExampleArrayDeque.java     # ArrayDeque — Stack e Queue eficiente
```

---

## Conceitos Abordados

### `List` — Coleção ordenada com índice

```java
List<User> users = new ArrayList<>();
users.add(new User("Ana", 25));
users.add(new User("Bob", 30));

// Ordenação com Comparator
users.sort(Comparator.comparing(User::getAge));

// Ordenação com Comparator customizado
users.sort(new CompareUserAge());

// Collections utility
Collections.sort(users, Comparator.comparing(User::getName).reversed());
```

### `Queue` e `Deque` — Fila e fila dupla

| Interface | Implementação | Característica |
|-----------|--------------|----------------|
| `Queue` | `PriorityQueue` | Heap binário — ordena por prioridade |
| `Queue` | `LinkedList` | Lista duplamente encadeada — FIFO |
| `Deque` | `ArrayDeque` | Array circular — Stack + Queue |

```java
// PriorityQueue — menor valor sai primeiro (min-heap)
Queue<User> pq = new PriorityQueue<>(Comparator.comparing(User::getAge));
pq.offer(new User("Carlos", 40));
pq.offer(new User("Diana", 20));
pq.poll(); // Diana (menor idade)

// ArrayDeque como Stack
Deque<String> stack = new ArrayDeque<>();
stack.push("primeiro");
stack.push("segundo");
stack.pop(); // "segundo"

// ArrayDeque como Queue
Deque<String> queue = new ArrayDeque<>();
queue.offer("A");
queue.offer("B");
queue.poll(); // "A"
```

---

## Comparator vs Comparable

| | `Comparable<T>` | `Comparator<T>` |
|---|---|---|
| Onde fica | Na própria classe | Classe separada ou lambda |
| Método | `compareTo(T o)` | `compare(T o1, T o2)` |
| Ordenações | 1 (natural) | N (múltiplas estratégias) |
| Uso | `Collections.sort(list)` | `list.sort(comparator)` |

```java
// Comparable — ordem natural
public class User implements Comparable<User> {
    public int compareTo(User other) {
        return Integer.compare(this.age, other.age);
    }
}

// Comparator — ordem alternativa
Comparator<User> byName = Comparator.comparing(User::getName);
Comparator<User> byAgeDesc = Comparator.comparing(User::getAge).reversed();
Comparator<User> combined = byName.thenComparing(byAgeDesc);
```

---

## Complexidade de Operações

| Operação | `ArrayList` | `LinkedList` | `PriorityQueue` | `ArrayDeque` |
|----------|-------------|--------------|-----------------|--------------|
| `add(E)` | O(1) amort. | O(1) | O(log n) | O(1) amort. |
| `get(i)` | O(1) | O(n) | — | — |
| `remove(i)` | O(n) | O(n) | O(log n) | O(1) |
| `contains` | O(n) | O(n) | O(n) | O(n) |

**Regra prática:**
- Use `ArrayList` para listas com acesso aleatório por índice
- Use `ArrayDeque` para filas/pilhas (mais eficiente que `LinkedList`)
- Use `PriorityQueue` quando precisar de ordenação automática na inserção

---

## Próximo Módulo

[mod12 — Collections: Array e Map](../mod12/README.md)
