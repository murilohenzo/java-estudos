# Padrões Comportamentais

Os padrões comportamentais concentram-se nas responsabilidades e interações entre objetos. Abaixo estão alguns dos padrões comportamentais mais conhecidos, com exemplos em Java:

## 1. **Chain of Responsibility Pattern:**

### Descrição:
O Chain of Responsibility Pattern passa solicitações ao longo de uma cadeia de manipuladores. Cada manipulador decide se processa a solicitação ou a passa para o próximo na cadeia.

### Exemplo em Java:
```java
abstract class Handler {
    protected Handler successor;

    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }

    public abstract void handleRequest(int request);
}

class ConcreteHandlerA extends Handler {
    public void handleRequest(int request) {
        if (request < 10) {
            System.out.println("Concrete Handler A: " + request);
        } else if (successor != null) {
            successor.handleRequest(request);
        }
    }
}

class ConcreteHandlerB extends Handler {
    public void handleRequest(int request) {
        if (request >= 10 && request < 20) {
            System.out.println("Concrete Handler B: " + request);
        } else if (successor != null) {
            successor.handleRequest(request);
        }
    }
}
```

### Utilização:
- Quando queremos evitar acoplamento explícito do remetente com receptores.
- Quando mais de um objeto pode lidar com uma solicitação e o manipulador não é conhecido a priori.

### Vantagens:
- Reduz o acoplamento entre remetente e destinatário.
- Permite que mais de um objeto tenha a chance de tratar uma solicitação.

### Desvantagens:
- Pode não garantir que a solicitação será tratada.
- A cadeia pode se tornar muito longa e afetar o desempenho.

## 2. **Command Pattern:**

### Descrição:
O Command Pattern encapsula uma solicitação como um objeto, permitindo a parametrização de clientes com diferentes solicitações, fila de solicitações e registro de solicitações.

### Exemplo em Java:
```java
interface Command {
    void execute();
}

class Receiver {
    public void action() {
        System.out.println("Receiver action");
    }
}

class ConcreteCommand implements Command {
    private Receiver receiver;

    public ConcreteCommand(Receiver receiver) {
        this.receiver = receiver;
    }

    public void execute() {
        receiver.action();
    }
}

class Invoker {
    private Command command;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void executeCommand() {
        command.execute();
    }
}
```

### Utilização:
- Quando queremos parametrizar objetos com operações.
- Quando queremos que as operações sejam tratadas, revertidas ou agrupadas.

### Vantagens:
- Desacopla o remetente do receptor.
- Permite a parametrização de objetos com operações.

### Desvantagens:
- Pode levar a uma explosão de classes de comando.
- Pode adicionar complexidade ao código.

## 3. **Interpreter Pattern:**

### Descrição:
O Interpreter Pattern define uma gramática para interpretar sentenças em uma linguagem específica.

### Exemplo em Java:
```java
interface Expression {
    boolean interpret(String context);
}

class TerminalExpression implements Expression {
    private String data;

    public TerminalExpression(String data) {
        this.data = data;
    }

    public boolean interpret(String context) {
        return context.contains(data);
    }
}

class OrExpression implements Expression {
    private Expression expr1;
    private Expression expr2;

    public OrExpression(Expression expr1, Expression expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    public boolean interpret(String context) {
        return expr1.interpret(context) || expr2.interpret(context);
    }
}
```

### Utilização:
- Quando há uma gramática simples que representa um conjunto de regras.
- Quando queremos interpretar expressões regulares ou linguagens específicas.

### Vantagens:
- Facilita a criação de interpretadores para linguagens específicas.
- Pode ser estendido facilmente com novas regras gramaticais.

### Desvantagens:
- Pode levar à criação de muitas classes.
- Não é adequado para gramáticas complexas.

## 4. **Iterator Pattern:**

### Descrição:
O Iterator Pattern fornece uma maneira de acessar sequencialmente os elementos de uma coleção sem expor sua representação subjacente.

### Exemplo em Java:
```java
interface Iterator {
    boolean hasNext();
    Object next();
}

interface Aggregate {
    Iterator createIterator();
}

class ConcreteIterator implements Iterator {
    private Object[] items;
    private int position = 0;

    public ConcreteIterator(Object[] items) {
        this.items = items;
    }

    public boolean hasNext() {
        return position < items.length;
    }

    public Object next() {
        return items[position++];
    }
}

class ConcreteAggregate implements Aggregate {
    private Object[] items;

    public ConcreteAggregate(Object[] items) {
        this.items = items;
    }

    public Iterator createIterator() {
        return new ConcreteIterator(items);
    }
}
```

### Utilização:
- Quando queremos percorrer elementos de uma coleção sem expor sua implementação.
- Quando queremos ter iterações diferentes para a mesma coleção.

### Vantagens:
- Desacopla o cliente da implementação da coleção.
- Permite iterações flexíveis sobre diferentes coleções.

### Desvantagens:
- A criação de iteradores pode ser complexa.
- Pode adicionar alguma sobrecarga ao código.

## 5. **Mediator Pattern:**

### Descrição:
O Mediator Pattern define um objeto que centraliza a comunicação entre objetos em um sistema.

### Exemplo em Java:
```java
interface Mediator {
    void sendMessage(String message, Colleague colleague);
}

class ConcreteMediator implements Mediator {
    private Colleague colleague1;
    private Colleague colleague2;

    public void setColleagues(Colleague colleague1, Colleague colleague2) {
        this.colleague1 = colleague1;
        this.colleague2 = colleague2;
    }

    public void sendMessage(String message, Colleague colleague) {
        if (colleague == colleague1) {
            colleague2.receiveMessage(message);
        } else {
            colleague1.receiveMessage(message);
        }
    }
}

abstract class Colleague {
    protected Mediator mediator;

    public Colleague(Mediator mediator) {
        this.mediator = mediator;
    }

    public abstract void sendMessage(String message);

    public abstract void receiveMessage(String message);
}

class ConcreteColleagueA extends Colleague {
    public ConcreteColleagueA(Mediator mediator) {
        super(mediator);
    }

    public void sendMessage(String message) {
        mediator.sendMessage(message, this);
    }

    public void receiveMessage(String message) {
        System.out.println("Concrete Colleague A received: " + message);
    }
}



class ConcreteColleagueB extends Colleague {
    public ConcreteColleagueB(Mediator mediator) {
        super(mediator);
    }

    public void sendMessage(String message) {
        mediator.sendMessage(message, this);
    }

    public void receiveMessage(String message) {
        System.out.println("Concrete Colleague B received: " + message);
    }
}
```

### Utilização:
- Quando queremos evitar que objetos se comuniquem diretamente entre si.
- Quando a comunicação entre objetos é complexa e pode ser centralizada.

### Vantagens:
- Desacopla objetos que se comunicam entre si.
- Facilita a manutenção e a extensão do sistema.

### Desvantagens:
- Pode introduzir um ponto único de falha.
- A centralização da comunicação pode levar a uma classe de mediador complexa.

Estes são apenas alguns dos padrões comportamentais mais comuns. Cada um tem suas próprias características e contextos de aplicação. A escolha do padrão a ser utilizado dependerá das necessidades específicas do projeto. Agora, vamos abordar alguns padrões adicionais: Observer, Template Method e Strategy.

## 6. **Observer Pattern:**

### Descrição:
O Observer Pattern define uma dependência um-para-muitos entre objetos, de modo que quando um objeto muda de estado, todos os seus dependentes são notificados e atualizados automaticamente.

### Exemplo em Java:
```java
import java.util.ArrayList;
import java.util.List;

interface Observer {
    void update(String message);
}

class ConcreteObserver implements Observer {
    private String name;

    public ConcreteObserver(String name) {
        this.name = name;
    }

    public void update(String message) {
        System.out.println(name + " received message: " + message);
    }
}

class Subject {
    private List<Observer> observers = new ArrayList<>();

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
}
```

### Utilização:
- Quando um objeto precisa notificar outros objetos sobre mudanças em seu estado.
- Quando há uma relação de dependência um-para-muitos entre objetos.

### Vantagens:
- Desacopla o objeto que emite a notificação dos objetos que a recebem.
- Suporta fácil adição/remoção de observadores.

### Desvantagens:
- Pode resultar em notificações ineficientes se muitos objetos estiverem observando.

## 7. **Template Method Pattern:**

### Descrição:
O Template Method Pattern define a estrutura de um algoritmo, permitindo que certas etapas sejam implementadas por subclasses, mas a estrutura geral seja mantida pelo próprio algoritmo.

### Exemplo em Java:
```java
abstract class AbstractClass {
    public void templateMethod() {
        step1();
        step2();
        step3();
    }

    protected abstract void step1();
    protected abstract void step2();

    private void step3() {
        System.out.println("AbstractClass: Step 3");
    }
}

class ConcreteClass extends AbstractClass {
    protected void step1() {
        System.out.println("ConcreteClass: Step 1");
    }

    protected void step2() {
        System.out.println("ConcreteClass: Step 2");
    }
}
```

### Utilização:
- Quando se deseja definir a estrutura de um algoritmo, mas permitir que as etapas específicas sejam implementadas por subclasses.
- Quando há partes de um algoritmo que são comuns em várias implementações, mas outras partes que podem variar.

### Vantagens:
- Define a estrutura geral do algoritmo, permitindo que as etapas específicas sejam alteradas por subclasses.
- Evita duplicação de código ao centralizar a lógica comum em uma classe base.

### Desvantagens:
- Pode levar a uma rigidez na estrutura do algoritmo.
- Subclasses são fortemente acopladas à estrutura da classe base.

## 8. **Strategy Pattern:**

### Descrição:
O Strategy Pattern define uma família de algoritmos, encapsula cada um deles e os torna intercambiáveis. Permite que o algoritmo varie independentemente dos clientes que o utilizam.

### Exemplo em Java:
```java
interface Strategy {
    void execute();
}

class ConcreteStrategyA implements Strategy {
    public void execute() {
        System.out.println("Concrete Strategy A");
    }
}

class ConcreteStrategyB implements Strategy {
    public void execute() {
        System.out.println("Concrete Strategy B");
    }
}

class Context {
    private Strategy strategy;

    public Context(Strategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void executeStrategy() {
        strategy.execute();
    }
}
```

### Utilização:
- Quando há famílias de algoritmos intercambiáveis.
- Quando se deseja evitar o uso de muitas instruções condicionais em um contexto.

### Vantagens:
- Permite que os algoritmos variem independentemente dos clientes.
- Facilita a adição de novos algoritmos sem modificar o código do cliente.

### Desvantagens:
- Pode introduzir muitas classes pequenas.
- Requer que o cliente compreenda as diferenças entre as estratégias.

Estes padrões comportamentais oferecem soluções flexíveis e eficazes para diferentes desafios de design em sistemas orientados a objetos. A escolha do padrão a ser utilizado dependerá das necessidades específicas do projeto e dos requisitos comportamentais do sistema.