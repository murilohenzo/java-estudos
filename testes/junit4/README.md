# Mod 23 — Testes com JUnit 4

> **Conceito central:** Fundamentos de teste automatizado com JUnit 4.  
> `@Test` · `Assert.*` · `@Before` · `@After` · `@BeforeClass` · `@AfterClass`

---

## Conteúdo

```
mod23/
├── src/main/java/
│   └── com/murilo/domain/Customer.java   # Entidade com nome, email, cpf
└── src/test/java/
    ├── CustomerTest.java                  # Testes básicos com Assert
    └── customer/StreamCustomerTest.java   # Testes de operações com Stream
```

---

## Anatomia de um Teste JUnit 4

```java
public class CustomerTest {

    // @Before — executado ANTES de cada @Test (setup)
    @Before
    public void setUp() {
        customer = new Customer("Ana", "ana@email.com", "123.456.789-00");
    }

    // @After — executado APÓS cada @Test (teardown)
    @After
    public void tearDown() {
        customer = null;
    }

    // @BeforeClass — executado UMA vez antes de todos os testes da classe
    @BeforeClass
    public static void setUpClass() {
        System.out.println("Iniciando suite de testes");
    }

    // @AfterClass — executado UMA vez após todos os testes
    @AfterClass
    public static void tearDownClass() {
        System.out.println("Suite encerrada");
    }

    // @Test — caso de teste
    @Test
    public void shouldCreateCustomerWithValidData() {
        assertNotNull(customer);
        assertEquals("Ana", customer.getName());
        assertEquals("ana@email.com", customer.getEmail());
    }

    // @Test(expected) — espera uma exceção específica
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNullName() {
        new Customer(null, "email@test.com", "000.000.000-00");
    }

    // @Test(timeout) — falha se demorar mais que N milissegundos
    @Test(timeout = 500)
    public void shouldCompleteWithinTimeout() {
        // operação que deve ser rápida
    }
}
```

---

## Assert — Métodos de Asserção

```java
// Igualdade
assertEquals("expected", "actual");
assertEquals(42, customer.getAge());
assertNotEquals("old value", customer.getName());

// Nulo
assertNull(customer.getNickname());
assertNotNull(customer);

// Booleano
assertTrue(customer.isActive());
assertFalse(customer.isDeleted());

// Arrays
assertArrayEquals(new int[]{1, 2, 3}, actual);

// Exceção (JUnit 4.13+)
assertThrows(CustomerNotFoundException.class, () ->
    service.findByCpf("999.999.999-99")
);

// Mensagem customizada (aparece no relatório de falha)
assertEquals("CPF deve ter 11 dígitos", 11, cpf.replaceAll("\\D", "").length());
```

---

## Ciclo de Vida dos Métodos

```
@BeforeClass (1x por classe)
    │
    ├── @Before (antes de cada @Test)
    │     │
    │     ├── @Test método 1
    │     │
    │   @After (após cada @Test)
    │
    ├── @Before
    │     │
    │     ├── @Test método 2
    │     │
    │   @After
    │
@AfterClass (1x por classe)
```

---

## Testando Streams com JUnit 4

```java
public class StreamCustomerTest {
    private List<Customer> customers;

    @Before
    public void setUp() {
        customers = List.of(
            new Customer("Ana",    "SP", 25),
            new Customer("Bob",    "RJ", 30),
            new Customer("Carlos", "SP", 22)
        );
    }

    @Test
    public void shouldFilterCustomersByCity() {
        List<Customer> spCustomers = customers.stream()
            .filter(c -> c.getCity().equals("SP"))
            .collect(Collectors.toList());

        assertEquals(2, spCustomers.size());
        assertTrue(spCustomers.stream().allMatch(c -> c.getCity().equals("SP")));
    }

    @Test
    public void shouldSumAgeOfAllCustomers() {
        int total = customers.stream()
            .mapToInt(Customer::getAge)
            .sum();

        assertEquals(77, total);
    }
}
```

---

## JUnit 4 vs JUnit 5

| Aspecto | JUnit 4 | JUnit 5 (Jupiter) |
|---------|---------|-------------------|
| Importação | `org.junit.*` | `org.junit.jupiter.api.*` |
| Setup | `@Before` | `@BeforeEach` |
| Teardown | `@After` | `@AfterEach` |
| Setup estático | `@BeforeClass` | `@BeforeAll` |
| Exceção esperada | `@Test(expected=Ex.class)` | `assertThrows(Ex.class, () -> ...)` |
| Mensagem custom | 1o parâmetro | Último parâmetro (Supplier) |
| Nested tests | ❌ | `@Nested` ✅ |
| Parametrized | `@RunWith(Parameterized.class)` | `@ParameterizedTest` ✅ |
| Extensões | Runner | Extension Model ✅ |

---

## Próximo Módulo

[mod24 — Testes: Service/DAO + Test Doubles](../mod24/README.md)
