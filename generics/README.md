# Mod 17 — Generics

> **Conceito central:** Parametrização de tipos para reutilização segura e sem casting.  
> Type Parameters · Bounded Wildcards · Generic Repository · Template Method + Generics

---

## Conteúdo

```
mod17/src/
├── generics/
│   ├── ExemploGenerics.java       # Sintaxe básica: <T>, <K,V>, métodos genéricos
│   └── fruta/
│       ├── Fruta.java             # Classe base
│       └── Maca.java              # extends Fruta
├── generics_project/              # CRUD de Clientes com Generic Repository
│   ├── domain/Client.java
│   ├── repository/
│   │   ├── generic/
│   │   │   ├── IGenericRepository.java          # Interface genérica CRUD
│   │   │   └── implementation/GenericRepository.java  # Template Method + HashMap
│   │   └── client/
│   │       ├── IClientsRepository.java          # extends IGenericRepository<Client>
│   │       └── implementation/ClientMapRepositoryInMemory.java
│   ├── useCases/ (CRUD)
│   └── application/App.java       # Swing UI
└── exercicio/
    ├── domain/
    │   ├── Car.java (interface)
    │   ├── SedanCar.java
    │   └── SmallCar.java
    └── repository/CarRepository.java  # extends GenericRepository<Car>
```

---

## Conceitos Abordados

### Sintaxe Básica de Generics

```java
// Classe genérica com type parameter T
public class Box<T> {
    private T value;
    public Box(T value) { this.value = value; }
    public T get() { return value; }
}

Box<String>  strBox = new Box<>("Java");
Box<Integer> intBox = new Box<>(42);

// Método genérico independente da classe
public static <T> List<T> repeat(T item, int times) {
    List<T> result = new ArrayList<>();
    for (int i = 0; i < times; i++) result.add(item);
    return result;
}

List<String> words = repeat("Java", 3);  // ["Java", "Java", "Java"]
```

### Type Bounds — Restrição de Tipo

```java
// Upper bound: T deve ser Fruta ou subclasse de Fruta
public <T extends Fruta> double totalPeso(List<T> frutas) {
    return frutas.stream().mapToDouble(Fruta::getPeso).sum();
}

// Múltiplos bounds
public <T extends Comparable<T> & Serializable> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}
```

### Wildcards

```java
// ? extends T — leitura (covariance): aceita T ou qualquer subclasse
public double sumAll(List<? extends Number> numbers) {
    return numbers.stream().mapToDouble(Number::doubleValue).sum();
}
sumAll(List.of(1, 2, 3));         // List<Integer>
sumAll(List.of(1.5, 2.5, 3.0));  // List<Double>

// ? super T — escrita (contravariance): aceita T ou qualquer superclasse
public void addNumbers(List<? super Integer> list) {
    list.add(1); list.add(2); list.add(3);
}
List<Number> numList = new ArrayList<>();
addNumbers(numList);  // funciona!

// PECS: Producer Extends, Consumer Super
// Se a lista produz T para você → ? extends T
// Se você produz T para a lista → ? super T
```

---

## Generic Repository (Template Method + Generics)

O `GenericRepository<T>` combina generics com o padrão Template Method
para fornecer CRUD reutilizável para qualquer entidade:

```java
// Interface genérica — T = tipo da entidade, E = tipo da chave
public interface IGenericRepository<T, E> {
    T save(T entity);
    Optional<T> findById(E id);
    List<T> findAll();
    T update(T entity);
    void delete(E id);
}

// Implementação genérica abstrata — Template Method
public abstract class GenericRepository<T, E> implements IGenericRepository<T, E> {
    protected final Map<E, T> store = new HashMap<>();

    // Template Method: subclasse informa como extrair a chave
    protected abstract E extractKey(T entity);

    @Override
    public T save(T entity) {
        store.put(extractKey(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(E id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }
}

// Repositório específico — implementa apenas extractKey()
public class ClientMapRepositoryInMemory extends GenericRepository<Client, Long> {
    @Override
    protected Long extractKey(Client client) {
        return client.getId();  // passo específico
    }
}

// Repositório de Carros — reutiliza TUDO de GenericRepository
public class CarRepository extends GenericRepository<Car, String> {
    @Override
    protected String extractKey(Car car) {
        return car.getPlate();
    }
}
```

---

## Type Erasure — Importante entender

```java
// Em runtime, os type parameters são apagados (type erasure)
List<String> strings = new ArrayList<>();
List<Integer> ints   = new ArrayList<>();

// Em bytecode, ambos são apenas List
strings.getClass() == ints.getClass();  // true

// Consequências:
// 1. Não pode fazer new T() — T desconhecido em runtime
// 2. Não pode fazer instanceof List<String>
// 3. Não pode criar arrays genéricos: new T[10] — ilegal
```

---

## Quando usar Generics

```
✅ Coleções homogêneas: List<User>, Map<Long, Order>
✅ Repositórios/DAOs reutilizáveis: GenericRepository<T, E>
✅ Utilitários: Pair<A, B>, Result<T, E>, Optional<T>
✅ API de callbacks: Function<T, R>, Predicate<T>
❌ Quando o tipo varia muito (use Object + instanceof)
❌ Tipos primitivos (use int[], não List<int>)
```

---

## Próximo Módulo

[mod18 — Annotations e Exceptions](../mod18/README.md)
