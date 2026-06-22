# Mod 25 — Projeto 02: Use Case Pattern + Maven + CI/CD

> **Projeto integrado** com arquitetura limpa, exceções de domínio tipadas, Maven e pipeline CI.  
> Use Cases · Repository Pattern · Custom Exceptions · JUnit 4 · GitHub Actions

---

## Visão Geral

Este projeto implementa um CRUD de Customers com foco em separação de responsabilidades clara,
exceções de domínio checked e pipeline de CI automatizado com GitHub Actions.

---

## Estrutura

```
mod25_projeto02/
├── pom.xml                                  # Maven: JUnit 4.13.2, Java 11
├── .github/workflows/ci.yaml               # GitHub Actions CI
│
├── src/br/com/murilo/
│   ├── domain/
│   │   └── Customer.java                   # Entidade com equals/hashCode por CPF
│   ├── repository/
│   │   ├── ICustomerRepository.java        # Contrato: findAll, save, find, delete, update
│   │   └── CustomerRepository.java         # HashMap in-memory — find() O(1) via map.get()
│   ├── usecases/
│   │   ├── CreateCustomerUseCase.java
│   │   ├── FindCustomerUseCase.java
│   │   ├── FindAllCustomersUseCase.java
│   │   ├── UpdateCustomerUseCase.java
│   │   └── DeleteCustomerUseCase.java
│   └── exceptions/
│       ├── CustomerAlreadyExistsException.java  # thrown em Create quando CPF existe
│       └── CustomerNotFoundException.java       # thrown em Find/Delete quando CPF não existe
│
└── src/test/java/br/com/murilo/customer/
    ├── CustomerServiceTest.java             # Testes de todos os use cases
    └── CustomerServiceTestSuite.java        # Suite JUnit 4
```

---

## Entidade Customer

```java
public class Customer {
    private String name;
    private String cpf;         // String — preserva zeros à esquerda (ex: "01234567890")
    private String phoneNumber; // String — preserva formatação
    private String address;
    private Integer number;
    private String city;
    private String state;

    // equals/hashCode por CPF (chave de negócio)
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Customer c)) return false;
        return Objects.equals(cpf, c.cpf);
    }

    @Override public int hashCode() { return Objects.hash(cpf); }
    @Override public String toString() { /* ... */ }
}
```

**Por que CPF é `String` e não `Long`?**  
CPFs com zeros à esquerda (ex: `01234567890`) seriam armazenados como `1234567890`,
perdendo o zero. String preserva o formato original.

---

## Repository — O(1) com HashMap

```java
public class CustomerRepository implements ICustomerRepository {
    private final Map<String, Customer> customers = new HashMap<>();

    @Override
    public Optional<Customer> find(String cpf) {
        return Optional.ofNullable(customers.get(cpf));  // O(1) — direto pelo CPF
    }

    @Override
    public Collection<Customer> findAll() {
        return Collections.unmodifiableCollection(customers.values()); // read-only
    }

    @Override
    public void delete(String cpf) {
        customers.remove(cpf);  // O(1)
    }
}
```

**Diferença vs implementação original:**  
O `find()` original usava `stream().filter()` — O(n). A chave do `HashMap` é o CPF,
permitindo lookup O(1) direto com `map.get(cpf)`.

---

## Use Cases

Cada Use Case tem **uma única responsabilidade** e recebe o repositório por injeção de construtor:

```java
// CreateCustomerUseCase — valida CPF único antes de salvar
public Customer handle(Customer customer) throws CustomerAlreadyExistsException {
    if (iCustomerRepository.find(customer.getCpf()).isPresent())
        throw new CustomerAlreadyExistsException("CPF já cadastrado: " + customer.getCpf());
    return iCustomerRepository.save(customer);
}

// FindCustomerUseCase — lança exceção tipada se não encontrado
public Customer handle(String cpf) throws CustomerNotFoundException {
    return iCustomerRepository.find(cpf)
        .orElseThrow(() -> new CustomerNotFoundException("CPF não encontrado: " + cpf));
}

// UpdateCustomerUseCase — delega busca ao FindCustomerUseCase (reuso)
public Customer handle(Customer updatedCustomer) throws CustomerNotFoundException {
    Customer existing = new FindCustomerUseCase(repository).handle(updatedCustomer.getCpf());
    repository.update(existing, updatedCustomer);
    return existing;
}

// DeleteCustomerUseCase — verifica existência antes de deletar
public void handle(String cpf) throws CustomerNotFoundException {
    new FindCustomerUseCase(customerRepository).handle(cpf); // valida existência
    customerRepository.delete(cpf);
}
```

---

## Exceções de Domínio

```java
// Checked — o caller é obrigado a tratar ou propagar
public class CustomerAlreadyExistsException extends Exception {
    public CustomerAlreadyExistsException(String message) { super(message); }
}

public class CustomerNotFoundException extends Exception {
    public CustomerNotFoundException(String message) { super(message); }
}
```

---

## Testes

```java
public class CustomerServiceTest {
    private static final String VALID_CPF  = "12345678910";
    private static final String ANOTHER_CPF= "98765432100";

    private Customer customer;
    private ICustomerRepository repository;

    @Before
    public void setUp() throws CustomerAlreadyExistsException {
        repository = new CustomerRepository();
        customer = new CreateCustomerUseCase(repository)
            .handle(new Customer("Murilo", VALID_CPF, "11987654321", "Rua X", 10, "Fortaleza", "CE"));
    }

    @Test
    public void whenExistingCustomerIsInformedThenShouldThrow() {
        assertThrows(CustomerAlreadyExistsException.class,
            () -> new CreateCustomerUseCase(repository).handle(/* mesmo CPF */));
    }

    @Test
    public void whenExistingCustomerIsUpdatedThenNameShouldChange() throws CustomerNotFoundException {
        var updated = new Customer("Henzo", VALID_CPF, ...);
        new UpdateCustomerUseCase(repository).handle(updated);
        assertEquals("Henzo", customer.getName());  // mutação in-place via repository.update()
    }
}
```

---

## CI/CD — GitHub Actions

```yaml
# .github/workflows/ci.yaml
name: ci-java-workflow

on:
  push:       { branches: ["main"] }
  pull_request:{ branches: ["main"] }

jobs:
  check-application:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: mod25_projeto02  # executa Maven dentro do módulo
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with: { distribution: corretto, java-version: '11', cache: maven }
      - name: Build
        run: mvn --batch-mode -DskipTests package
      - name: Test
        run: mvn --batch-mode -Dmaven.test.failure.ignore=true test
      - name: Report
        uses: dorny/test-reporter@v1
        with:
          path: mod25_projeto02/target/surefire-reports/*.xml
          reporter: java-junit
```

### Como executar localmente

```bash
cd mod25_projeto02
mvn test
# Relatório em: target/surefire-reports/
```

---

## Próximo Módulo

[mod25\_projeto02\_v01 — Projeto 02 v1: Generic DAO com Reflection](../mod25_projeto02_v01/README.md)
