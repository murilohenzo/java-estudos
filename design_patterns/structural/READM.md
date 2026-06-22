# Padrões Estruturais

Os padrões estruturais são projetados para facilitar a composição de classes e objetos para formar estruturas maiores e mais complexas. Abaixo estão alguns dos padrões estruturais mais conhecidos, com exemplos em Java:

## 1. **Adapter Pattern:**

### Descrição:
O Adapter Pattern permite que a interface de uma classe existente seja usada como uma interface esperada por outra classe.

### Exemplo em Java:
```java
class Adaptee {
    public void specificRequest() {
        System.out.println("Adaptee's specific request");
    }
}

interface Target {
    void request();
}

class Adapter implements Target {
    private Adaptee adaptee;

    public Adapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }

    public void request() {
        adaptee.specificRequest();
    }
}
```

### Utilização:
- Quando você precisa integrar código legado com novas funcionalidades.
- Ao trabalhar com bibliotecas ou classes que possuem interfaces incompatíveis.

### Vantagens:
- Permite a cooperação entre classes com interfaces incompatíveis.
- Ajuda na reutilização de classes existentes sem modificar seu código.

### Desvantagens:
- Pode levar a um grande número de adaptadores.
- Adição de complexidade ao código.

## 2. **Bridge Pattern:**

### Descrição:
O Bridge Pattern separa uma abstração de sua implementação, permitindo que ambas possam variar independentemente.

### Exemplo em Java:
```java
interface Implementor {
    void operationImpl();
}

class ConcreteImplementorA implements Implementor {
    public void operationImpl() {
        System.out.println("Concrete Implementor A");
    }
}

class ConcreteImplementorB implements Implementor {
    public void operationImpl() {
        System.out.println("Concrete Implementor B");
    }
}

abstract class Abstraction {
    protected Implementor implementor;

    public Abstraction(Implementor implementor) {
        this.implementor = implementor;
    }

    public abstract void operation();
}

class RefinedAbstraction extends Abstraction {
    public RefinedAbstraction(Implementor implementor) {
        super(implementor);
    }

    public void operation() {
        implementor.operationImpl();
    }
}
```

### Utilização:
- Quando se quer evitar uma ligação permanente entre uma abstração e sua implementação.
- Ao ter variações em ambas abstração e implementação independentemente.

### Vantagens:
- Desacopla a abstração da implementação.
- Permite que as hierarquias de abstração e implementação evoluam independentemente.

### Desvantagens:
- Aumenta a complexidade do código.

## 3. **Composite Pattern:**

### Descrição:
O Composite Pattern é composto por objetos individuais e suas composições para representar hierarquias parte-todo.

### Exemplo em Java:
```java
import java.util.ArrayList;
import java.util.List;

abstract class Component {
    public abstract void operation();
}

class Leaf extends Component {
    public void operation() {
        System.out.println("Leaf operation");
    }
}

class Composite extends Component {
    private List<Component> children = new ArrayList<>();

    public void add(Component component) {
        children.add(component);
    }

    public void remove(Component component) {
        children.remove(component);
    }

    public void operation() {
        System.out.println("Composite operation");
        for (Component child : children) {
            child.operation();
        }
    }
}
```

### Utilização:
- Ao lidar com objetos que podem ser agrupados em estruturas hierárquicas.
- Quando se quer tratar objetos individuais e composições de objetos de maneira uniforme.

### Vantagens:
- Facilita a criação de estruturas complexas.
- Permite que clientes tratem objetos individuais e composições de maneira uniforme.

### Desvantagens:
- Pode ser mais difícil de entender em comparação com estruturas simples.
- Pode resultar em uma árvore complexa e difícil de gerenciar.

## 4. **Decorator Pattern:**

### Descrição:
O Decorator Pattern anexa responsabilidades adicionais a um objeto de forma dinâmica.

### Exemplo em Java:
```java
interface Component {
    void operation();
}

class ConcreteComponent implements Component {
    public void operation() {
        System.out.println("Concrete Component");
    }
}

abstract class Decorator implements Component {
    protected Component component;

    public Decorator(Component component) {
        this.component = component;
    }

    public void operation() {
        component.operation();
    }
}

class ConcreteDecoratorA extends Decorator {
    public ConcreteDecoratorA(Component component) {
        super(component);
    }

    public void operation() {


        super.operation();
        System.out.println("Concrete Decorator A");
    }
}

class ConcreteDecoratorB extends Decorator {
    public ConcreteDecoratorB(Component component) {
        super(component);
    }

    public void operation() {
        super.operation();
        System.out.println("Concrete Decorator B");
    }
}
```

### Utilização:
- Quando se quer adicionar funcionalidades a objetos de forma dinâmica.
- Ao evitar a criação de subclasses para cada combinação de funcionalidades.

### Vantagens:
- Permite adicionar responsabilidades dinamicamente.
- Oferece uma alternativa à criação de subclasses.

### Desvantagens:
- Pode resultar em muitos objetos pequenos que são difíceis de entender.
- Pode introduzir complexidade e potencial overhead.

## 5. **Facade Pattern:**

### Descrição:
O Facade Pattern oferece uma interface unificada para um conjunto de objetos que constituem um subsistema.

### Exemplo em Java:
```java
class SubsystemA {
    public void operationA() {
        System.out.println("Subsystem A operation");
    }
}

class SubsystemB {
    public void operationB() {
        System.out.println("Subsystem B operation");
    }
}

class Facade {
    private SubsystemA subsystemA;
    private SubsystemB subsystemB;

    public Facade() {
        this.subsystemA = new SubsystemA();
        this.subsystemB = new SubsystemB();
    }

    public void operation() {
        subsystemA.operationA();
        subsystemB.operationB();
    }
}
```

### Utilização:
- Ao simplificar a interface de um conjunto de classes complexas.
- Quando se quer fornecer uma interface única para um subsistema.

### Vantagens:
- Esconde a complexidade do sistema.
- Facilita a utilização do sistema ao fornecer uma interface mais simples.

### Desvantagens:
- Pode não ser adequado se precisar de um controle mais fino sobre os subsistemas.
- A mudança no subsistema pode afetar a fachada.

Estes são alguns dos padrões estruturais mais comuns. A escolha do padrão a ser utilizado dependerá das necessidades específicas do projeto, sendo importante considerar as características e contextos de aplicação de cada um.