# Padrões Comportamentais (Behavioral Patterns)

> Padrões comportamentais lidam com **algoritmos e a atribuição de responsabilidades**
> entre objetos — como eles se comunicam e colaboram em runtime.

---

## Padrões implementados

| Padrão | Demo | Intenção resumida |
|--------|------|-------------------|
| [Chain of Responsibility](./ChainOfResponsabilityWithStrategy/) | `ChainOfResponsabilityWithStrategy/src/` | Passa requisição por cadeia de handlers |
| [Command](./Command/) | `Command/src/CommandDemo.java` | Encapsula requisição como objeto — undo/redo, filas |
| [Iterator](./Iterator/) | `Iterator/src/IteratorDemo.java` | Acesso sequencial sem expor estrutura interna |
| [Mediator](./Mediator/) | `Mediator/src/MediatorDemo.java` | Centraliza comunicação entre objetos — elimina acoplamento N×N |
| [Memento](./Memento/) | `Memento/src/MementoDemo.java` | Captura e restaura estado sem violar encapsulamento |
| [Observer](./Observer/) | `Observer/src/` | Notifica assinantes sobre eventos de um subject |
| [State](./State/) | `State/src/StateDemo.java` | Altera comportamento quando estado interno muda |
| [Strategy](./Strategy/) | `Strategy/src/StrategyDemo.java` | Família de algoritmos intercambiáveis em runtime |
| [Template Method](./TemplateMethod/) | `TemplateMethod/src/` | Esqueleto de algoritmo em classe base; detalhes em subclasses |
| [Visitor](./Visitor/) | `Visitor/src/VisitorDemo.java` | Adiciona operações a objetos sem modificá-los |

---

## Guia de escolha

```
Cadeia de validadores/handlers?                 → Chain of Responsibility
Undo/Redo, job queue, transações?               → Command
Percorrer coleção sem expor estrutura?          → Iterator
Muitos objetos se comunicam entre si?           → Mediator
Salvar/restaurar estado (snapshot)?             → Memento
Notificar múltiplos objetos sobre evento?       → Observer
Comportamento muda com estado interno?          → State
Algoritmo varia; contexto permanece estável?    → Strategy
Esqueleto de algoritmo; partes variam?          → Template Method
Adicionar operação a hierarquia sem modificá-la? → Visitor
```

---

## Comparativo: Strategy vs State vs Template Method

Todos lidam com variação de comportamento — a diferença é a **dimensão da variação**:

```java
// Template Method — herança, partes do algoritmo sobrescritas
abstract class Worker {
    final void doWork() { prepare(); work(); cleanup(); }  // esqueleto fixo
    abstract void work();  // variação via herança
}

// Strategy — composição, algoritmo inteiro trocado em runtime
class Sorter {
    private SortStrategy strategy;  // pode ser trocada
    void sort(List<?> data) { strategy.sort(data); }
}

// State — objeto muda comportamento conforme estado interno
class Order {
    private OrderState state;  // Draft, Pending, Paid...
    void pay() { state = state.pay(this); }  // state controla transição
}
```

---

## Comparativo: Observer vs Mediator

```java
// Observer — unidirecional, sujeito não conhece observadores
journalist.addObserver(emailObserver);   // N observadores independentes
journalist.publish("Breaking news");     // dispara para todos

// Mediator — bidirecional, central de coordenação
ana.sendTo("Bob", "Oi");   // Ana não conhece Bob — fala com o Mediator
// Mediator encaminha para Bob
```

---

## Command vs Memento para Undo

| Aspecto | Command | Memento |
|---|---|---|
| Como desfaz | Operação reversa (`undo()`) | Snapshot do estado completo |
| Memória | O(operações) — pode ser pequeno | O(estado) — pode ser grande |
| Granularidade | Fina (cada ação) | Grossa (estado inteiro) |
| Melhor para | Editores com muitas operações | Objetos complexos com estado difícil de reconstruir |
