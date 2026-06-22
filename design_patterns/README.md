# design_patterns — Padrões de Projeto GOF (Gang of Four)

> Implementação completa dos **23 padrões GOF** em Java 21.  
> Cada padrão tem um demo autocontido com exemplos práticos, Javadoc técnico,
> e uso de features modernas (records, sealed, pattern matching, lambdas).

---

## Índice

- [Criacionais (5/5)](#criacionais--creational)
- [Estruturais (7/7)](#estruturais--structural)
- [Comportamentais (10/10)](#comportamentais--behavioral)
- [Guia de escolha rápida](#guia-de-escolha-rápida)
- [Relação com Java 21](#relação-com-java-21)

---

## Criacionais — Creational

> Lidam com a criação de objetos, tornando-a mais flexível e reutilizável.

| Padrão | Localização | Intenção | Quando usar |
|--------|-------------|----------|-------------|
| **Singleton** | `creational/Singleton/` | Uma única instância global com ponto de acesso controlado | Config, connection pool, logger — quando só 1 instância faz sentido |
| **Factory Method** | `creational/FactoryMethod/` | Define interface para criar objetos; subclasses decidem qual classe instanciar | Criação depende do contexto/tipo; desacoplar cliente da implementação |
| **Abstract Factory** | `creational/AbstractFactory/` | Cria famílias de objetos relacionados sem especificar classes concretas | Kits de UI (Windows/Mac), ambientes de teste vs produção |
| **Builder** | `creational/Builder/` | Constrói objetos complexos passo a passo; separa construção de representação | Objetos com muitos parâmetros opcionais (ex: HTTP Request, SQL Query) |
| **Prototype** | `creational/Prototype/` | Cria novos objetos copiando um protótipo existente | Quando instanciação é cara; clonagem de templates |

### Exemplos de código — Criacionais

```java
// ── Singleton (Bill Pugh — thread-safe sem synchronized) ──
class Config {
    private Config() {}
    private static class Holder { static final Config INSTANCE = new Config(); }
    static Config instance() { return Holder.INSTANCE; }
}

// ── Builder com record JDK 21 ──
record HttpRequest(String url, String method, Map<String, String> headers, String body) {
    static Builder builder(String url) { return new Builder(url); }
    static class Builder { /* setters fluentes; build() cria o record */ }
}

// ── Factory Method ──
interface Transport { void deliver(); }
class TransportFactory {
    static Transport create(TransportType type) {
        return switch (type) {
            case TRUCK -> new Truck();
            case SHIP  -> new Ship();
        };
    }
}
```

---

## Estruturais — Structural

> Lidam com a composição de classes e objetos para formar estruturas maiores.

| Padrão | Localização | Intenção | Quando usar |
|--------|-------------|----------|-------------|
| **Adapter** | `structural/Adapter/` | Converte interface incompatível na interface esperada pelo cliente | Integrar biblioteca legada; compatibilidade entre APIs |
| **Bridge** | `structural/Bridge/` | Separa abstração de implementação — ambas variam independentemente | Hierarquia em 2 dimensões (ex: Notificação × Canal de envio) |
| **Composite** | `structural/Composite/` | Árvore de objetos — trata folhas e composições uniformemente | File system, menus hierárquicos, UI trees, expressões |
| **Decorator** | `structural/Decorator/` | Adiciona responsabilidades dinamicamente sem herança | Cross-cutting: logging, cache, auth, compressão — Java IO usa isso |
| **Facade** | `structural/Facade/` | Interface simplificada para subsistema complexo | Simplificar API de biblioteca; reduzir acoplamento com subsistemas |
| **Flyweight** | `structural/Flyweight/` | Compartilha estado intrínseco entre muitos objetos | Partículas, caracteres de texto, ícones — quando RAM é crítica |
| **Proxy** | `structural/Proxy/` | Substituto que controla acesso ao objeto real | Lazy loading, cache, proteção, logging, remote access |

### Exemplos de código — Estruturais

```java
// ── Bridge — Notification × Channel ──
abstract class Notification {
    protected final Channel channel;  // bridge
    abstract void notify(String recipient, String msg);
}
// UrgentNotification(new SmsChannel())  — qualquer combinação

// ── Composite — File System ──
sealed interface FileSystemNode permits FileNode, DirectoryNode {
    long sizeInBytes();
    void print(String indent);
}

// ── Decorator — empilhamento de comportamentos ──
DataSource src = new InMemoryDataSource();
DataSource enc = new EncodingDecorator(src);
DataSource cmp = new CompressionDecorator(enc);
DataSource log = new LoggingDecorator(cmp);  // cada camada adiciona uma responsabilidade

// ── Proxy — Caching ──
class CachingProxy implements CourseRepository {
    String find(String code) {
        return cache.computeIfAbsent(code, real::find);  // transparente ao cliente
    }
}
```

---

## Comportamentais — Behavioral

> Lidam com algoritmos e a atribuição de responsabilidades entre objetos.

| Padrão | Localização | Intenção | Quando usar |
|--------|-------------|----------|-------------|
| **Chain of Responsibility** | `behavioral/ChainOfResponsabilityWithStrategy/` | Passa requisição por cadeia; cada handler decide se processa ou passa adiante | Middleware pipeline, validadores em cascata, event bubbling |
| **Command** | `behavioral/Command/` | Encapsula requisição como objeto com execute/undo | Undo/redo, job queues, transações, macros gravadas |
| **Iterator** | `behavioral/Iterator/` | Acesso sequencial a coleção sem expor sua estrutura | Estruturas não-padrão (árvore, grafo), paginação, lazy streams |
| **Mediator** | `behavioral/Mediator/` | Centraliza comunicação — objetos não se conhecem diretamente | Chat room, formulários UI com dependências, ATC |
| **Memento** | `behavioral/Memento/` | Captura e restaura estado sem violar encapsulamento | Checkpoints de jogo, snapshots de configuração, rollback |
| **Observer** | `behavioral/Observer/` | Notifica assinantes sobre eventos de um subject | Event systems, GUI listeners, pub/sub, reactive streams |
| **State** | `behavioral/State/` | Objeto muda comportamento quando estado interno muda | Pedido/conexão/semáforo — máquinas de estado |
| **Strategy** | `behavioral/Strategy/` | Família de algoritmos intercambiáveis em runtime | Descontos, ordenação, roteamento, validação — elimina if/else |
| **Template Method** | `behavioral/TemplateMethod/` | Esqueleto de algoritmo na classe base; partes delegadas às subclasses | Quando o fluxo é fixo mas os detalhes variam por tipo |
| **Visitor** | `behavioral/Visitor/` | Adiciona operações a hierarquia sem modificá-la | Export em formatos (HTML/PDF/MD), análise de AST, relatórios |

### Exemplos de código — Comportamentais

```java
// ── Strategy como lambda ──
@FunctionalInterface interface DiscountStrategy { double apply(Order o); }
processor.setStrategy(o -> o.subtotal() > 1000 ? o.subtotal() * 0.05 : 0.0);

// ── Command com Undo ──
sealed interface EditorCommand permits InsertCommand, DeleteCommand {
    void execute(); void undo();
}
history.execute(new InsertCommand(editor, 0, "Hello"));
history.undo();  // restaura estado anterior

// ── State com Sealed ──
sealed interface OrderState permits Draft, Pending, Paid, Shipped, Delivered, Cancelled {}
// switch exaustivo — compilador garante todos os casos
String action = switch (state) {
    case Paid paid -> "Preparar envio";
    // ... sem default necessário
};

// ── Visitor com double dispatch ──
element.accept(htmlExporter);    // dispatch 1: tipo do element
element.accept(mdExporter);      // mesmo element, visitor diferente
element.accept(statsCollector);  // 3 operações sem modificar os elements

// ── Memento com Record ──
record GameSnapshot(GameState state, Instant savedAt, String label) {}
var snap = game.save("Checkpoint 1");  // record imutável
game.restore(snap);                    // restaura exatamente aquele estado
```

---

## Guia de escolha rápida

### Por problema

| Problema | Padrão(s) |
|---|---|
| Instanciar objeto complexo | Builder, Factory Method, Abstract Factory |
| Uma instância apenas | Singleton |
| Clonar objeto existente | Prototype |
| Adaptar API incompatível | Adapter |
| Separar abstração de implementação | Bridge |
| Estrutura recursiva/parte-todo | Composite |
| Adicionar comportamento sem herança | Decorator |
| Simplificar subsistema complexo | Facade |
| Muitos objetos similares, pouca RAM | Flyweight |
| Controlar acesso a objeto | Proxy |
| Cadeia de handlers | Chain of Responsibility |
| Desfazer operações | Command (operações), Memento (estado) |
| Percorrer estrutura não-padrão | Iterator |
| Reduzir dependências N×N | Mediator |
| Notificar múltiplos objetos | Observer |
| Comportamento muda com estado | State |
| Algoritmo varia em runtime | Strategy |
| Esqueleto fixo, detalhes variáveis | Template Method |
| Nova operação em hierarquia estável | Visitor |

---

## Relação com Java 21

| Feature Java 21 | Padrões que simplifica |
|---|---|
| `record` (imutável) | Builder (product), Memento (snapshot), Flyweight (intrínseco), Command (dados) |
| `sealed interface` | State (exaustividade de estados), Visitor (Element hierarchy), Composite |
| Pattern matching `switch` | Strategy (dispatch), State (transições), Visitor alternativo |
| `@FunctionalInterface` | Strategy (1 método = lambda), Command (execute = lambda), Observer |
| Lambdas / method refs | Strategy, Command, Observer, Iterator |
| `var` | Reduz verbosidade em todos |

---

## Referência

Baseado em: [refactoring.guru/pt-br/design-patterns](https://refactoring.guru/pt-br/design-patterns)

> **"Os padrões não são algoritmos específicos ou implementações concretas.
> São conceitos de mais alto nível para organizar o seu código."** — Refactoring Guru
