# Padrões Estruturais (Structural Patterns)

> Padrões estruturais lidam com a **composição de classes e objetos** para formar
> estruturas maiores enquanto mantêm essas estruturas flexíveis e eficientes.

---

## Padrões implementados

| Padrão | Demo | Intenção resumida |
|--------|------|-------------------|
| [Adapter](./Adapter/) | `Adapter/src/` | Converte interface incompatível em outra esperada pelo cliente |
| [Bridge](./Bridge/) | `Bridge/src/BridgeDemo.java` | Separa abstração de implementação — ambas variam independentemente |
| [Composite](./Composite/) | `Composite/src/CompositeDemo.java` | Árvore de objetos — trata folhas e composições uniformemente |
| [Decorator](./Decorator/) | `Decorator/src/DecoratorDemo.java` | Adiciona responsabilidades dinamicamente — alternativa à herança |
| [Facade](./Facade/) | `Facade/src/` | Interface simplificada para subsistema complexo |
| [Flyweight](./Flyweight/) | `Flyweight/src/FlyweightDemo.java` | Compartilha estado intrínseco entre muitos objetos |
| [Proxy](./Proxy/) | `Proxy/src/ProxyDemo.java` | Controla acesso ao objeto real (cache, auth, lazy load) |

---

## Guia de escolha

```
Precisa adaptar interface legada?               → Adapter
Hierarquia cresce em 2 dimensões independentes? → Bridge
Estrutura parte-todo (árvore)?                  → Composite
Adicionar comportamento sem herança?            → Decorator
Simplificar API complexa?                       → Facade
Muitos objetos similares, memória cara?         → Flyweight
Controlar acesso / lazy / cache / segurança?    → Proxy
```

---

## Comparativo rápido

| Padrão | Wrapping? | Muda interface? | Quando criado? | Propósito |
|--------|-----------|-----------------|----------------|-----------|
| Adapter | Sim | Sim (converte) | retroativo | Compatibilidade |
| Bridge | Não (compõe) | Não | projetado | Variação independente |
| Composite | Não | Não | projetado | Árvore parte-todo |
| Decorator | Sim | Não | em runtime | Adicionar comportamento |
| Facade | Não | Sim (simplifica) | projetado | Usabilidade |
| Flyweight | Não | Não | projetado | Memória |
| Proxy | Sim | Não | projetado/runtime | Controle de acesso |

---

## Decorator vs Proxy vs Adapter

Todos os três "envolvem" um objeto — a diferença é a **intenção**:

```java
// Adapter — converte interface incompatível
class MediaAdapter implements MediaPlayer {  // interface esperada
    private final AdvancedPlayer adaptee;    // interface incompatível
}

// Decorator — adiciona comportamento; mesma interface
class LoggingDecorator implements DataSource {
    private final DataSource wrappee;  // mesma interface
    void write(String data) { log(); wrappee.write(data); }
}

// Proxy — controla acesso; mesma interface; gerencia lifecycle
class CachingProxy implements CourseRepository {
    private final CourseRepository real;  // mesma interface
    String find(String code) { return cache.getOrElse(code, () -> real.find(code)); }
}
```
