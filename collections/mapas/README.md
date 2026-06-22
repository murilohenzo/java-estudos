# Mod 12 — Collections: Array e Map

> **Conceito central:** Arrays multidimensionais e estruturas chave-valor.  
> `HashMap` · `TreeMap` · `LinkedHashMap` · Arrays bidimensionais · `Map.Entry`

---

## Conteúdo

```
mod12/
├── array/
│   └── Main.java              # Arrays primitivos e multidimensionais
├── collection_map/
│   ├── Main.java              # HashMap, LinkedHashMap — operações básicas
│   └── TreeMapExamples.java   # TreeMap — ordem natural e Comparator
├── exercicio/
│   ├── User.java              # Entidade com nome e telefone
│   └── Main.java              # Map<String, User> como agenda de contatos
└── exercicio01/
    └── Main.java              # Frequência de palavras com HashMap
```

---

## Conceitos Abordados

### Arrays Multidimensionais

```java
// Array bidimensional — matriz 3x3
int[][] matrix = new int[3][3];
matrix[0][0] = 1;

// Inicialização literal
int[][] grid = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

// Iteração com for-each aninhado
for (int[] row : grid) {
    for (int val : row) {
        System.out.print(val + " ");
    }
}

// Array jagged (linhas com tamanhos diferentes)
int[][] jagged = new int[3][];
jagged[0] = new int[]{1};
jagged[1] = new int[]{2, 3};
jagged[2] = new int[]{4, 5, 6};
```

### Implementações de `Map`

| Implementação | Ordem | Thread-safe | Null key | Complexidade get/put |
|---------------|-------|-------------|----------|----------------------|
| `HashMap` | Nenhuma | ❌ | ✅ (1x) | O(1) amort. |
| `LinkedHashMap` | Inserção | ❌ | ✅ (1x) | O(1) amort. |
| `TreeMap` | Natural/Comparator | ❌ | ❌ | O(log n) |
| `ConcurrentHashMap` | Nenhuma | ✅ | ❌ | O(1) amort. |
| `Hashtable` | Nenhuma | ✅ (legacy) | ❌ | O(1) amort. |

```java
// HashMap — lookup O(1), sem ordem garantida
Map<String, User> contacts = new HashMap<>();
contacts.put("11987654321", new User("Ana", "11987654321"));

// getOrDefault — evita NullPointerException
User user = contacts.getOrDefault("00000000000", new User("Desconhecido", ""));

// computeIfAbsent — cria entrada se ausente (thread-safe em ConcurrentHashMap)
contacts.computeIfAbsent("11999999999", k -> new User("Novo", k));

// TreeMap — mantém chaves em ordem natural (Comparable)
TreeMap<String, Integer> freq = new TreeMap<>();
freq.put("banana", 3);
freq.put("abacaxi", 1);
freq.firstKey(); // "abacaxi" — menor chave

// LinkedHashMap com acesso-order (LRU Cache simplificado)
LinkedHashMap<String, String> lru = new LinkedHashMap<>(16, 0.75f, true);
```

### Iteração sobre Map

```java
// EntrySet — acesso à chave e valor em uma iteração
for (Map.Entry<String, User> entry : contacts.entrySet()) {
    System.out.printf("Tel: %s → %s%n", entry.getKey(), entry.getValue().getName());
}

// Java 8+ forEach com lambda
contacts.forEach((tel, user) -> System.out.println(tel + " → " + user));

// Stream sobre values
contacts.values().stream()
    .filter(u -> u.getName().startsWith("A"))
    .forEach(System.out::println);
```

### Contagem de frequência (padrão clássico)

```java
// Contar frequência de palavras
String text = "java é uma linguagem java poderosa";
Map<String, Integer> freq = new HashMap<>();

for (String word : text.split(" ")) {
    freq.merge(word, 1, Integer::sum);
    // Equivalente a: freq.put(word, freq.getOrDefault(word, 0) + 1);
}

// Palavra mais frequente
freq.entrySet().stream()
    .max(Map.Entry.comparingByValue())
    .ifPresent(e -> System.out.println("Mais frequente: " + e.getKey()));
```

---

## Quando usar cada Map

```
Precisa de ordem por chave (A, B, C...)?    → TreeMap
Precisa de ordem de inserção?               → LinkedHashMap
Precisa de performance máxima (O(1))?       → HashMap
Precisa de acesso concorrente thread-safe?  → ConcurrentHashMap
```

---

## Próximo Módulo

[mod13 — OOP: Interfaces, Classes Abstratas, Composição](../mod13/README.md)
