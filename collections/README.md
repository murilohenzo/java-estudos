# collections — Java Collections Framework

> Cobre toda a API de coleções da JDK: estruturas lineares (List, Queue, Deque) e
> estruturas de mapeamento (Array multidimensional, Map, TreeMap, LinkedHashMap).

---

## Submódulos

| Diretório | Conteúdo | Complexidade-chave |
|---|---|---|
| [`lineares/`](./lineares/README.md) | `ArrayList`, `PriorityQueue`, `LinkedList`, `ArrayDeque` | Acesso O(1) vs inserção O(n) |
| [`mapas/`](./mapas/README.md) | Arrays multidimensionais, `HashMap`, `TreeMap`, `LinkedHashMap` | Hash O(1) vs Tree O(log n) |

---

## Por que Collections importam

```
Escolha errada de estrutura → complexidade algorítmica inadequada
Escolha certa → código mais simples e performático

Exemplo:
  busca por CPF em List → O(n)  — percorre um a um
  busca por CPF em Map  → O(1)  — acessa pelo hash diretamente
```

---

## Comparativo rápido

| Estrutura | Acesso | Busca | Inserção | Ordenação | Uso típico |
|---|---|---|---|---|---|
| `ArrayList` | O(1) | O(n) | O(1)† | O(n log n) | Acesso por índice |
| `LinkedList` | O(n) | O(n) | O(1) | O(n log n) | Fila/deque frequente |
| `ArrayDeque` | O(1)† | O(n) | O(1)† | — | Pilha/fila de alta perf |
| `PriorityQueue` | O(1) min | O(n) | O(log n) | — | Menor elemento primeiro |
| `HashMap` | — | O(1)† | O(1)† | — | Chave → valor rápido |
| `LinkedHashMap` | — | O(1)† | O(1)† | inserção | Mapa com ordem garantida |
| `TreeMap` | — | O(log n) | O(log n) | natural | Mapa ordenado por chave |

† amortizado

---

## Guia de referência

Para análise detalhada de complexidade de cada operação, consulte:  
[guia_engenharia/COMPLEXIDADE_ALGORITMOS.md](../guia_engenharia/COMPLEXIDADE_ALGORITMOS.md)
