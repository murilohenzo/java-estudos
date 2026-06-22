# Mod 24 — Testes: Service/DAO com Test Doubles e Suites

> **Conceito central:** Testes isolados com Mock Objects e organização em Test Suites.  
> DAO Pattern · Service Layer · Test Doubles · `@RunWith(Suite.class)` · Injeção via construtor

---

## Conteúdo

```
mod24/
├── src/br/com/murilo/
│   ├── domain/
│   │   ├── Customer.java    # Entidade com id, name, email
│   │   └── Contract.java    # Entidade com id, description, customerId
│   ├── dao/
│   │   ├── ICustomerDAO.java    # Interface DAO
│   │   ├── IContractDAO.java
│   │   └── implement/
│   │       └── CustomerDAO.java # DAO in-memory (List)
│   └── service/
│       ├── IContractsService.java
│       ├── ContractService.java
│       └── CustomerService.java
│
└── test/br/com/murilo/
    ├── customer/
    │   ├── CustomerTest.java      # Testes de CustomerService
    │   └── CustomerSuiteTest.java # Suite de testes
    └── contract/
        └── ContractSuiteTest.java
```

---

## Arquitetura Service/DAO

```
Controller / UseCase
       │ depende de
       ▼
  ICustomerService
       │ implementado por
       ▼
  CustomerService ──► ICustomerDAO
                           │ implementado por
                           ▼
                      CustomerDAO (produção)
                      CustomerDAOMock (testes)
```

A injeção do DAO via construtor permite substituir a implementação real por um mock em testes, sem framework de DI.

---

## DAO Pattern

```java
// Interface — contrato de persistência
public interface ICustomerDAO {
    Customer save(Customer customer);
    Optional<Customer> findById(int id);
    List<Customer> findAll();
    Customer update(Customer customer);
    void delete(int id);
}

// Implementação de produção
public class CustomerDAO implements ICustomerDAO {
    private final List<Customer> store = new ArrayList<>();
    private int nextId = 1;

    @Override
    public Customer save(Customer customer) {
        customer.setId(nextId++);
        store.add(customer);
        return customer;
    }

    @Override
    public Optional<Customer> findById(int id) {
        return store.stream().filter(c -> c.getId() == id).findFirst();
    }
}
```

---

## Service Layer

```java
public class CustomerService {
    private final ICustomerDAO customerDAO;  // injetado via construtor

    public CustomerService(ICustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public Customer save(Customer customer) {
        validateCustomer(customer);          // lógica de negócio no service
        return customerDAO.save(customer);
    }

    public Customer findById(int id) {
        return customerDAO.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException("ID: " + id));
    }

    private void validateCustomer(Customer c) {
        if (c.getName() == null || c.getName().isBlank())
            throw new IllegalArgumentException("Nome não pode ser vazio");
    }
}
```

### ContractService corrigido

```java
public class ContractService implements IContractsService {
    private final IContractDAO iContractDAO;

    public ContractService(IContractDAO iContractDAO) {
        this.iContractDAO = iContractDAO;
    }

    // ✅ Retorna resultado do DAO (antes retornava null)
    @Override public String   save()                    { return iContractDAO.save(); }
    @Override public Contract find(int id)              { return iContractDAO.find(id); }
    @Override public void     remove(int id)            { iContractDAO.remove(id); }
    @Override public Contract update(Contract contract) { return iContractDAO.update(contract); }
}
```

---

## Test Doubles

**Test Double** é um objeto que substitui uma dependência real durante testes.

| Tipo | Características |
|------|----------------|
| **Dummy** | Passado como argumento; nunca usado |
| **Stub** | Retorna respostas fixas pré-programadas |
| **Mock** | Verifica que métodos foram chamados com argumentos certos |
| **Spy** | Mock que também chama a implementação real |
| **Fake** | Implementação simplificada que funciona de verdade (in-memory DB) |

```java
// CustomerDAOMock — Fake (implementação in-memory simplificada)
public class CustomerDAOMock implements ICustomerDAO {
    private final List<Customer> customers = new ArrayList<>();

    @Override
    public Customer save(Customer c) {
        customers.add(c);
        return c;
    }

    @Override
    public Optional<Customer> findById(int id) {
        return customers.stream().filter(c -> c.getId() == id).findFirst();
    }

    // Implementações mínimas para os demais métodos...
}
```

---

## Teste com Test Double

```java
public class CustomerTest {
    private CustomerService service;
    private ICustomerDAO    mockDao;

    @Before
    public void setUp() {
        mockDao = new CustomerDAOMock();           // Test Double injetado
        service = new CustomerService(mockDao);    // DI via construtor
    }

    @Test
    public void shouldSaveAndRetrieveCustomer() {
        Customer customer = new Customer("Ana", "ana@test.com");
        Customer saved    = service.save(customer);

        assertNotNull(saved.getId());
        assertEquals("Ana", service.findById(saved.getId()).getName());
    }

    @Test(expected = CustomerNotFoundException.class)
    public void shouldThrowWhenNotFound() {
        service.findById(9999);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectEmptyName() {
        service.save(new Customer("", "email@test.com"));
    }
}
```

---

## Test Suites

Agrupa múltiplas classes de teste para execução em conjunto:

```java
// Suite que executa CustomerTest E ContractSuiteTest
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CustomerTest.class,
    ContractSuiteTest.class
})
public class CustomerSuiteTest {
    // classe vazia — apenas serve como ponto de entrada da suite
}
```

---

## Próximo Módulo

[mod25\_projeto02 — Projeto 02: Use Case Pattern + CI/CD](../mod25_projeto02/README.md)
