# Mod 15/16 — Design Patterns GOF (Gang of Four)

> **23 padrões clássicos** categorizados em Criacionais, Estruturais e Comportamentais.  
> Cada padrão resolve um problema de design recorrente sem reinventar a roda.

---

## Padrões Implementados

```
mod15_mod16/
├── creational/           # Padrões de Criação — como objetos são criados
│   ├── Singleton/        # Uma única instância global
│   ├── FactoryMethod/    # Subclasse decide qual objeto criar
│   ├── AbstractFactory/  # Família de objetos relacionados
│   ├── Builder/          # Construção passo-a-passo de objetos complexos
│   └── Prototype/        # Clonagem de objetos existentes
├── structural/           # Padrões Estruturais — como objetos se organizam
│   ├── Adapter/          # Adapta interface incompatível
│   └── Facade/           # Interface simplificada para subsistema complexo
├── behavioral/           # Padrões Comportamentais — como objetos se comunicam
│   ├── Observer/         # Notificação pub/sub
│   ├── TemplateMethod/   # Esqueleto de algoritmo com passos variáveis
│   └── ChainOfResponsabilityWithStrategy/  # Cadeia + estratégia de handlers
└── exercicio01/          # Exercício: fábricas de carros por região
```

---

## Padrões Criacionais

### Singleton — Instância única garantida

**Problema:** garantir que uma classe tenha exatamente uma instância (ex: pool de conexões, logger).

```java
// Thread-safe com inicialização lazy (Bill Pugh Holder pattern)
public class Singleton {
    private Singleton() {}

    private static final class Holder {
        static final Singleton INSTANCE = new Singleton();
    }

    public static Singleton getInstance() {
        return Holder.INSTANCE;  // thread-safe sem synchronized
    }
}

// Singleton com Properties (arquivo de configuração)
public class SingletonProperties {
    private static SingletonProperties instance;
    private final Properties props;

    private SingletonProperties() {
        props = new Properties();
        // carrega application.properties
    }

    public static synchronized SingletonProperties getInstance() {
        if (instance == null) instance = new SingletonProperties();
        return instance;
    }
}
```

**Variações implementadas:** `Singleton`, `SingletonProperties`, `SingletonPropertiesThread`, `SingletonPropertiesSync`

---

### Factory Method — Subclasse decide o tipo

**Problema:** criar objetos sem expor lógica de criação ao cliente.

```java
// Produto abstrato
public interface ITransport { void deliver(); }

// Fábrica abstrata
public abstract class TransportFactory {
    public abstract ITransport createTransport();  // Factory Method

    // Template Method que usa o Factory Method
    public void planDelivery() {
        ITransport transport = createTransport();
        transport.deliver();
    }
}

// Fábrica concreta
public class TruckFactory extends TransportFactory {
    @Override
    public ITransport createTransport() { return new Truck(); }
}
```

---

### Abstract Factory — Família de produtos

**Problema:** criar famílias de objetos relacionados sem especificar classes concretas.

```java
// Fábrica abstrata
public interface IFactory {
    ITransport createTransport();
    IContract  createContract();
}

// Família Brasília
public class ContratosFactory implements IFactory {
    @Override public ITransport createTransport() { return new BrasiliaCar(); }
    @Override public IContract  createContract()  { return new BrasiliaContract(); }
}

// Família Sem Contratos
public class SemContratosFactory implements IFactory {
    @Override public ITransport createTransport() { return new CorolaCar(); }
    @Override public IContract  createContract()  { return new NoContract(); }
}

// Cliente — depende apenas da interface IFactory
public class Client {
    Client(IFactory factory) {
        var transport = factory.createTransport();
        var contract  = factory.createContract();
    }
}
```

---

### Builder — Construção passo a passo

**Problema:** construir objetos complexos com muitos parâmetros opcionais de forma legível.

```java
// Builder fluente
Burger burger = new BurgerBuilder()
    .type(BurgerType.VEGGIE)
    .withCheese()
    .withLettuce()
    .withTomato()
    .build();

// Director — encapsula receitas fixas
Director director = new Director(new BurgerBuilder());
Burger classic = director.buildClassicBurger();
Burger veggie  = director.buildVeggieBurger();
```

---

### Prototype — Clonar ao invés de construir do zero

**Problema:** criar novos objetos copiando instâncias existentes (útil quando criação é custosa).

```java
public abstract class ReportPrototype implements Cloneable {
    protected String title;
    protected List<String> data;

    @Override
    public ReportPrototype clone() {
        try {
            ReportPrototype copy = (ReportPrototype) super.clone();
            copy.data = new ArrayList<>(this.data);  // deep copy da lista
            return copy;
        } catch (CloneNotSupportedException e) { throw new AssertionError(); }
    }
}

// Uso: clone é mais rápido que criar+popular do zero
ReportPrototype original = reportRegistry.getTemplate("monthly");
ReportPrototype copy = original.clone();
copy.setTitle("Março 2026");
```

---

## Padrões Estruturais

### Adapter — Adaptador de interface

**Problema:** integrar componentes com interfaces incompatíveis (ex: biblioteca legada).

```java
// Interface esperada pelo cliente
public interface MediaPlayer {
    void play(String format, String file);
}

// Classe legada com interface diferente
public class LegacyMediaPlayer {
    public void playVlc(String file) { /* ... */ }
    public void playMp4(String file) { /* ... */ }
}

// Adapter — traduz a interface nova para a legada
public class AdvancedAudioPlayerAdapter implements MediaPlayer {
    private final LegacyMediaPlayer legacy = new LegacyMediaPlayer();

    @Override
    public void play(String format, String file) {
        switch (format.toLowerCase()) {
            case "vlc" -> legacy.playVlc(file);
            case "mp4" -> legacy.playMp4(file);
            default    -> throw new UnsupportedOperationException("Format: " + format);
        }
    }
}
```

---

### Facade — Simplificação de subsistema

**Problema:** subsistema complexo com muitas classes; o cliente não deveria conhecer todas.

```java
// Subsistema complexo
class Amplifier    { void on() {} void setVolume(int v) {} }
class DvdPlayer    { void on() {} void play(String m) {}  }
class Projector    { void on() {} void setInput(String s){} }
class SurroundSound{ void on() {} void setMode(String m) {} }

// Facade — interface simplificada
public class HomeTheater {
    private final Amplifier amp;
    private final DvdPlayer dvd;
    private final Projector proj;
    private final SurroundSound sound;

    public void watchMovie(String movie) {
        amp.on();    amp.setVolume(10);
        dvd.on();    dvd.play(movie);
        proj.on();   proj.setInput("DVD");
        sound.on();  sound.setMode("surround");
    }
}
```

---

## Padrões Comportamentais

### Observer — Pub/Sub reativo

**Problema:** notificar múltiplos objetos quando o estado de um objeto muda.

```java
// Subject (Publisher)
public class Journalist {
    private final List<Observer> observers = new ArrayList<>();
    private String news;

    public void attach(Observer o) { observers.add(o); }
    public void detach(Observer o) { observers.remove(o); }

    public void setNews(String news) {
        this.news = news;
        observers.forEach(o -> o.update(news));  // notifica todos
    }
}

// Observers (Subscribers)
new EmailObserver("dev@example.com").register(journalist);
new TVObserver("Canal News").register(journalist);
new SMSObserver("11999999999").register(journalist);

// Trigger
journalist.setNews("Java 21 released!");  // todos os observers são notificados
```

---

### Template Method — Algoritmo com passos variáveis

**Problema:** definir o esqueleto de um algoritmo; subclasses implementam passos específicos.

```java
// Classe abstrata define o algoritmo
public abstract class Worker {
    // Template Method — sequência fixa
    public final void doWork() {
        wakeUp();
        eat();
        commute();   // passo variável
        work();      // passo variável
        goHome();
    }

    protected abstract void commute();
    protected abstract void work();

    private void wakeUp() { System.out.println("Acordei"); }
    // ...
}

// Bombeiro
public class Firefighter extends Worker {
    @Override protected void commute() { System.out.println("Caminhão de bombeiros"); }
    @Override protected void work()    { System.out.println("Apagando incêndios"); }
}
```

---

### Chain of Responsibility + Strategy

**Problema:** processar uma mensagem por uma cadeia de handlers, cada um com estratégia própria.

```java
// Handler abstrato da cadeia
public abstract class MessageHandler {
    private MessageHandler next;

    public MessageHandler setNext(MessageHandler next) {
        this.next = next;
        return next;
    }

    public abstract void handle(Message message);

    protected void passToNext(Message message) {
        if (next != null) next.handle(message);
    }
}

// Cadeia: Text → GIF → BroadcastMessage
MessageHandler textHandler = new MessageTextHandler();
MessageHandler gifHandler  = new MessageGIFHandler();
MessageHandler broadcast   = new BroadcastMessage();

textHandler.setNext(gifHandler).setNext(broadcast);
textHandler.handle(new Message("Hello!", MessageType.TEXT));
```

---

## Exercício — Fábricas de Carros por Região

```java
// Abstract Factory + Factory Method combinados
public interface CarFactory {
    Car createSmallCar();
    Car createSedanCar();
    Car createLuxuryCar();
}

public class AsianCarFactory implements CarFactory  { /* Toyota, Honda */ }
public class DefaultCarFactory  implements CarFactory  { /* VW, Fiat */ }

// Enum para seleção
Car car = switch (location) {
    case ASIA    -> new AsianCarFactory().createSmallCar();
    case DEFAULT -> new DefaultCarFactory().createSmallCar();
};
```

---

## Resumo — Quando usar cada padrão

| Padrão | Problema que resolve |
|--------|---------------------|
| Singleton | Instância única compartilhada (config, pool, cache) |
| Factory Method | Criar objetos sem saber a classe concreta antecipadamente |
| Abstract Factory | Família de produtos que devem ser usados juntos |
| Builder | Objeto com muitos campos opcionais; construção legível |
| Prototype | Cópia de objetos é mais barata que criação do zero |
| Adapter | Integrar componente com interface incompatível |
| Facade | Simplificar API de subsistema complexo |
| Observer | Notificação de múltiplos objetos em mudança de estado |
| Template Method | Algoritmo com estrutura fixa e passos variáveis |
| Chain of Responsibility | Pipeline de processamento com handlers opcionais |

---

## Próximo Módulo

[mod17 — Generics](../mod17/README.md)
