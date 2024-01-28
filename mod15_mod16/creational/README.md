# Padrões de Criação

Os padrões de projeto criacionais são fundamentais para o desenvolvimento de software, pois visam facilitar e tornar mais flexível o processo de criação de objetos. Abaixo, apresento alguns dos padrões criacionais mais conhecidos, juntamente com exemplos em Java, além de discutir quando e onde utilizá-los, assim como suas vantagens e desvantagens.

## 1. **Singleton Pattern:**

### Descrição:
O Singleton Pattern garante que uma classe tenha apenas uma instância e fornece um ponto global de acesso a essa instância.

### Exemplo em Java:
```java
public class Singleton {
    private static Singleton instance;

    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}
```

### Utilização:
- Quando é necessário ter exatamente uma instância de uma classe.
- Em situações onde uma única instância global é mais eficiente.

### Vantagens:
- Garante uma única instância da classe.
- Fornece um ponto global de acesso.

### Desvantagens:
- Não é adequado para ambientes concorrentes sem modificações.
- Pode introduzir acoplamento global no código.

## 2. **Factory Method Pattern:**

### Descrição:
O Factory Method Pattern define uma interface para criar um objeto, permitindo que as subclasses alterem o tipo de objetos que serão criados.

### Exemplo em Java:
```java
interface Product {
    void display();
}

class ConcreteProduct implements Product {
    public void display() {
        System.out.println("Concrete Product");
    }
}

abstract class Creator {
    public abstract Product factoryMethod();
}

class ConcreteCreator extends Creator {
    public Product factoryMethod() {
        return new ConcreteProduct();
    }
}
```

### Utilização:
- Quando uma classe não pode antecipar a classe dos objetos que precisa criar.
- Quando uma classe quer delegar a responsabilidade de criação para suas subclasses.

### Vantagens:
- Permite a extensão de classes sem modificar o código existente.
- Desacopla a criação de objetos de sua utilização.

### Desvantagens:
- Pode levar à criação de muitas subclasses.
- Aumenta a complexidade do código.

## 3. **Abstract Factory Pattern:**

### Descrição:
O Abstract Factory Pattern fornece uma interface para criar famílias de objetos relacionados ou dependentes sem especificar suas classes concretas.

### Exemplo em Java:
```java
interface AbstractProductA {
    void display();
}

class ConcreteProductA1 implements AbstractProductA {
    public void display() {
        System.out.println("Concrete Product A1");
    }
}

interface AbstractProductB {
    void display();
}

class ConcreteProductB1 implements AbstractProductB {
    public void display() {
        System.out.println("Concrete Product B1");
    }
}

interface AbstractFactory {
    AbstractProductA createProductA();
    AbstractProductB createProductB();
}

class ConcreteFactory1 implements AbstractFactory {
    public AbstractProductA createProductA() {
        return new ConcreteProductA1();
    }

    public AbstractProductB createProductB() {
        return new ConcreteProductB1();
    }
}
```

### Utilização:
- Quando um sistema deve ser independente de como seus objetos são criados, compostos e representados.
- Quando o sistema é configurado com várias famílias de objetos.

### Vantagens:
- Isola as classes concretas.
- Facilita a troca de famílias de produtos.

### Desvantagens:
- Dificulta a adição de novos produtos.
- Pode levar a um grande número de classes.

## 4. **Builder Pattern:**

### Descrição:
O Builder Pattern separa a construção de um objeto complexo de sua representação, permitindo a mesma construção resultar em diferentes representações.

### Exemplo em Java:
```java
class Product {
    private String part1;
    private String part2;

    public void setPart1(String part1) {
        this.part1 = part1;
    }

    public void setPart2(String part2) {
        this.part2 = part2;
    }

    public void display() {
        System.out.println("Part 1: " + part1 + ", Part 2: " + part2);
    }
}

interface Builder {
    void buildPart1();
    void buildPart2();
    Product getResult();
}

class ConcreteBuilder implements Builder {
    private Product product = new Product();

    public void buildPart1() {
        product.setPart1("Builder Part 1");
    }

    public void buildPart2() {
        product.setPart2("Builder Part 2");
    }

    public Product getResult() {
        return product;
    }
}

class Director {
    public Product construct(Builder builder) {
        builder.buildPart1();
        builder.buildPart2();
        return builder.getResult();
    }
}
```

### Utilização:
- Quando a construção de um objeto é complexa e há necessidade de diversos formatos ou representações.
- Quando queremos isolar o código de construção de um objeto do código que o utiliza.

### Vantagens:
- Permite a criação de objetos complexos passo a passo.
- Isola o código de construção do código de representação.

### Desvantagens:
- Aumenta a complexidade do código.
- Pode exigir a criação de várias classes para representar diferentes produtos.

## 5. **Prototype Pattern:**

### Descrição:
O Prototype Pattern cria novos objetos por meio da cópia de um objeto existente, conhecido como protótipo.

### Exemplo em Java:
```java
class Prototype implements Cloneable {
    private String attribute;

    public Prototype(String attribute) {
        this.attribute = attribute;
    }

    public String getAttribute() {
        return attribute;
    }

    public Prototype clone() {
        try {
            return (Prototype) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
```

### Utilização:
- Quando a criação de um objeto é mais custosa em termos de desempenho ou complexidade.
- Quando se deseja evitar a criação de objetos repetitivamente.

### Vantagens:
- Reduz o custo de criação de objetos.
- Facilita a criação de objetos com estados iniciais específicos.

### Desvantagens:
- Pode ser necessário implementar a interface `Cloneable`.
- A cópia superficial pode ser inadequada em alguns casos.

Estes são alguns dos padrões criacionais mais comuns. A escolha do padrão a ser utilizado dependerá das necessidades específicas do projeto, sendo importante considerar as características e contextos de aplicação de cada um.