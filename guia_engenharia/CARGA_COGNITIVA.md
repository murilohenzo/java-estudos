# Carga Cognitiva e Design Simples

> **"A simplicidade é a sofisticação máxima."** — Leonardo da Vinci  
> **"Código é lido muito mais do que escrito."** — Robert C. Martin

---

## Índice

- [O que é Carga Cognitiva?](#o-que-é-carga-cognitiva)
- [Os 3 tipos de carga cognitiva](#os-3-tipos-de-carga-cognitiva)
- [Princípio 1 — Nomes que revelam intenção](#princípio-1--nomes-que-revelam-intenção)
- [Princípio 2 — Funções pequenas, uma responsabilidade](#princípio-2--funções-pequenas-uma-responsabilidade)
- [Princípio 3 — Evitar aninhamento profundo](#princípio-3--evitar-aninhamento-profundo)
- [Princípio 4 — Imutabilidade reduz contexto mental](#princípio-4--imutabilidade-reduz-contexto-mental)
- [Princípio 5 — Menos parâmetros, mais clareza](#princípio-5--menos-parâmetros-mais-clareza)
- [Princípio 6 — Evitar números mágicos e flags booleanas](#princípio-6--evitar-números-mágicos-e-flags-booleanas)
- [Princípio 7 — Java 21 reduz boilerplate e carga visual](#princípio-7--java-21-reduz-boilerplate-e-carga-visual)
- [Anti-padrões de alta carga cognitiva](#anti-padrões-de-alta-carga-cognitiva)
- [Aplicação no projeto](#aplicação-no-projeto)

---

## O que é Carga Cognitiva?

**Carga cognitiva** é a quantidade de esforço mental necessário para entender um trecho de código.
Quanto maior a carga, mais tempo leva para:
- Ler e compreender
- Encontrar bugs
- Fazer modificações com confiança
- Onboardar novos desenvolvedores

O objetivo do design simples não é escrever menos código, mas **escrever código que exige menos
esforço mental para ser entendido**.

---

## Os 3 tipos de carga cognitiva

| Tipo | Descrição | Causa comum em código |
|---|---|---|
| **Intrínseca** | Complexidade inerente do domínio | Regra de negócio complexa |
| **Extrínseca** | Complexidade desnecessária da implementação | Código confuso, mal nomeado |
| **Germane** | Esforço para formar novos padrões mentais | Aprendizado de novas abstrações |

> **Nosso objetivo:** eliminar a carga **extrínseca**. A intrínseca é inevitável; a germane é valiosa.

---

## Princípio 1 — Nomes que revelam intenção

O nome deve responder: **por que existe? o que faz? como é usado?**

```java
// ❌ Alta carga — o que é d? o que é c? o que retorna?
int d(Customer c) {
    return (int) ChronoUnit.DAYS.between(c.dt(), LocalDate.now());
}

// ✅ Baixa carga — autoexplicativo
int daysSinceLastPurchase(Customer customer) {
    return (int) ChronoUnit.DAYS.between(customer.lastPurchaseDate(), LocalDate.now());
}
```

```java
// ❌ Nomes enganosos — lista, mas retorna apenas um
List<Customer> getCustomer(String cpf) { ... }

// ✅ Nome correto
Optional<Customer> findCustomerByCpf(String cpf) { ... }
```

```java
// ❌ Abreviações obscuras
var cTot = calcTotAmt(ords);

// ✅ Expressivo
var orderTotal = calculateTotalAmount(orders);
```

### Regras práticas

- **Variáveis:** substantivos que descrevem o que contêm (`customerList`, não `list`)
- **Métodos:** verbos que descrevem o que fazem (`findById`, `calculateTotal`, `isExpired`)
- **Booleanos:** prefixo `is/has/can/should` (`isActive`, `hasPermission`, `canDelete`)
- **Classes:** substantivos no singular (`Customer`, não `Customers`, não `CustomerManager`)

---

## Princípio 2 — Funções pequenas, uma responsabilidade

**Uma função deve fazer exatamente uma coisa, fazê-la bem, e somente ela.**

```java
// ❌ Função que faz tudo — difícil de testar, modificar e entender
void processOrder(Order order, Customer customer, Inventory inventory) {
    // valida order
    if (order.items().isEmpty()) throw new IllegalArgumentException("...");
    if (order.total() <= 0) throw new IllegalArgumentException("...");

    // verifica estoque
    for (var item : order.items()) {
        if (!inventory.hasStock(item.productId(), item.quantity()))
            throw new StockException("...");
    }

    // calcula desconto
    double discount = customer.isPremium() ? 0.1 : 0.0;
    double finalTotal = order.total() * (1 - discount);

    // persiste
    orderRepository.save(order.withTotal(finalTotal));
    inventory.decrement(order.items());
    emailService.sendConfirmation(customer.email(), order);
}
```

```java
// ✅ Cada função tem uma responsabilidade clara
void processOrder(Order order, Customer customer) {
    validateOrder(order);
    checkInventory(order.items());
    var finalTotal = applyDiscount(order.total(), customer);
    persistAndNotify(order.withTotal(finalTotal), customer);
}

private void validateOrder(Order order) {
    if (order.items().isEmpty())  throw new IllegalArgumentException("Order must have items");
    if (order.total() <= 0)       throw new IllegalArgumentException("Order total must be positive");
}

private double applyDiscount(double total, Customer customer) {
    double rate = customer.isPremium() ? 0.10 : 0.0;
    return total * (1 - rate);
}
```

### Métricas de referência

| Métrica | Alerta | Ação |
|---|---|---|
| Linhas por método | > 20 | extrair método |
| Parâmetros | > 3 | usar objeto/record |
| Nível de aninhamento | > 3 | inverter condições, extrair |
| Ciclomatic complexity | > 10 | decompor |

---

## Princípio 3 — Evitar aninhamento profundo

Aninhamento profundo (`if` dentro de `if` dentro de `for`) é a maior fonte de
carga extrínseca — o leitor precisa rastrear múltiplos contextos simultaneamente.

### Guard Clauses (retorno antecipado)

```java
// ❌ "Arrow anti-pattern" — nesting profundo
String processPayment(Payment payment) {
    if (payment != null) {
        if (payment.isValid()) {
            if (payment.amount() > 0) {
                if (customerService.exists(payment.customerId())) {
                    return doProcess(payment);
                } else {
                    return "Customer not found";
                }
            } else {
                return "Invalid amount";
            }
        } else {
            return "Invalid payment";
        }
    } else {
        return "Payment is null";
    }
}

// ✅ Guard clauses — retorno antecipado para casos inválidos
String processPayment(Payment payment) {
    if (payment == null)                          return "Payment is null";
    if (!payment.isValid())                       return "Invalid payment";
    if (payment.amount() <= 0)                    return "Invalid amount";
    if (!customerService.exists(payment.customerId())) return "Customer not found";

    return doProcess(payment);  // lógica principal sem aninhamento
}
```

### Stream em vez de loops aninhados

```java
// ❌ Nested loops — O(n²) e alta carga cognitiva
List<String> premiumEmails = new ArrayList<>();
for (Customer c : customers) {
    if (c.isPremium()) {
        for (Order o : c.orders()) {
            if (o.total() > 1000) {
                premiumEmails.add(c.email());
                break;
            }
        }
    }
}

// ✅ Stream — declarativo, sem aninhamento explícito
List<String> premiumEmails = customers.stream()
    .filter(Customer::isPremium)
    .filter(c -> c.orders().stream().anyMatch(o -> o.total() > 1000))
    .map(Customer::email)
    .toList();
```

---

## Princípio 4 — Imutabilidade reduz contexto mental

Objetos mutáveis obrigam o leitor a rastrear **todas as possíveis modificações** ao longo do código.  
Objetos imutáveis têm estado garantido — são mais fáceis de raciocinar.

```java
// ❌ Classe mutável — qual é o estado de customer neste ponto?
Customer customer = new Customer();
customer.setName("Ana");
customer.setCpf("123.456.789-00");
// ... 50 linhas depois ...
customer.setName("Ana Silva");  // mutação inesperada
service.save(customer);         // qual name foi salvo?
```

```java
// ✅ Record imutável — estado é definitivo na construção
record Customer(String cpf, String name, String email) {
    Customer {
        Objects.requireNonNull(cpf,   "cpf is required");
        Objects.requireNonNull(name,  "name is required");
        Objects.requireNonNull(email, "email is required");
    }

    // Modificação retorna nova instância — original não muda
    Customer withName(String newName) {
        return new Customer(this.cpf, newName, this.email);
    }
}

// Uso — sem surpresas
var customer  = new Customer("123.456.789-00", "Ana", "ana@email.com");
var renamed   = customer.withName("Ana Silva");  // original intacto
service.save(renamed);
```

### Coleções imutáveis

```java
// ❌ Lista mutável retornada — caller pode modificar estado interno
List<Customer> findAll() {
    return this.customers;  // referência ao estado interno!
}

// ✅ Visão imutável
List<Customer> findAll() {
    return Collections.unmodifiableList(this.customers);
    // ou: return List.copyOf(this.customers);
}
```

---

## Princípio 5 — Menos parâmetros, mais clareza

Métodos com muitos parâmetros são difíceis de chamar corretamente — é fácil
trocar a ordem ou passar valores errados.

```java
// ❌ 6 parâmetros — qual é o email? qual é o phone?
User createUser(String firstName, String lastName, String email,
                String phone, String cpf, boolean active) { ... }

// Chamada — completamente opaca
createUser("Ana", "Silva", "ana@email.com", "11999999999", "123.456.789-00", true);
```

```java
// ✅ Builder pattern — cada campo é nomeado explicitamente
User user = User.builder()
    .firstName("Ana")
    .lastName("Silva")
    .email("ana@email.com")
    .phone("11999999999")
    .cpf("123.456.789-00")
    .active(true)
    .build();
```

```java
// ✅ Record como parâmetro agrupado — quando vários parâmetros pertencem juntos
record CreateUserCommand(
    String firstName,
    String lastName,
    String email,
    String phone,
    String cpf
) {}

User createUser(CreateUserCommand cmd, boolean active) { ... }
```

### Regra de Miller — 7 ± 2

A memória de trabalho humana processa **5 a 9 itens** simultaneamente.  
Métodos com mais de 3-4 parâmetros excedem essa capacidade — encapsule em objetos.

---

## Princípio 6 — Evitar números mágicos e flags booleanas

```java
// ❌ O que é 30? O que significa true?
if (user.age() > 30 && canAccess(user, true)) { ... }

// ✅ Constants nomeadas e enums revelam intenção
static final int MINIMUM_AGE_FOR_PREMIUM = 30;

if (user.age() > MINIMUM_AGE_FOR_PREMIUM && canAccess(user, AccessLevel.FULL)) { ... }
```

```java
// ❌ Booleano como parâmetro — o que é false aqui?
processOrder(order, false);

// ✅ Enum explícito
enum OrderMode { IMMEDIATE, SCHEDULED }
processOrder(order, OrderMode.IMMEDIATE);
```

---

## Princípio 7 — Java 21 reduz boilerplate e carga visual

Java 21 introduziu ferramentas que reduzem carga extrínseca diretamente:

### Records — eliminam boilerplate de POJO

```java
// ❌ Java 8 — 30+ linhas para um simples DTO
class CourseDTO {
    private final String code;
    private final String name;
    // constructor, getters, equals, hashCode, toString...
}

// ✅ Java 21 record — 1 linha, tudo gerado
record CourseDTO(String code, String name) {}
```

### Pattern Matching — elimina casts explícitos

```java
// ❌ Java 8
if (obj instanceof String) {
    String s = (String) obj;  // cast redundante — já sabemos que é String
    return s.toUpperCase();
}

// ✅ Java 21
if (obj instanceof String s) {
    return s.toUpperCase();  // s já está tipado e em escopo
}
```

### switch com arrow — elimina fall-through bugs

```java
// ❌ Java 8 switch — fall-through acidental
switch (status) {
    case ACTIVE:
        process();    // e também executa o próximo se esquecer break!
    case INACTIVE:
        log();
    default:
        throw new IllegalStateException();
}

// ✅ Java 21 switch expression
var result = switch (status) {
    case ACTIVE   -> process();
    case INACTIVE -> log();
};  // exaustivo por sealed — sem fall-through possível
```

### var — reduz redundância visual

```java
// ❌ Redundante
HashMap<String, List<Order>> ordersByCustomer = new HashMap<String, List<Order>>();

// ✅ var — tipo inferido, ainda tipado estaticamente
var ordersByCustomer = new HashMap<String, List<Order>>();
```

### Text Blocks — SQL/JSON legível inline

```java
// ❌ Concatenação ilegível
String sql = "SELECT c.id, c.name, r.status " +
             "FROM courses c " +
             "JOIN registrations r ON c.id = r.course_id " +
             "WHERE c.code = ?";

// ✅ Text block — indentação natural, sem escapes
String sql = """
    SELECT c.id, c.name, r.status
    FROM courses c
    JOIN registrations r ON c.id = r.course_id
    WHERE c.code = ?
    """;
```

---

## Anti-padrões de alta carga cognitiva

| Anti-padrão | Problema | Solução |
|---|---|---|
| God class | 1 classe faz tudo | Decompor por responsabilidade |
| Long method | > 20 linhas, múltiplas coisas | Extrair métodos privados |
| Arrow code | > 3 níveis de nesting | Guard clauses, extração |
| Magic numbers | `if (x > 30)` | Constantes nomeadas |
| Boolean parameters | `process(order, true, false)` | Enums, objetos de configuração |
| Mutable shared state | Difícil rastrear | Imutabilidade, records |
| Comentário que explica o quê | O código deveria ser legível | Renomear, simplificar |
| Comentário desatualizado | Contradiz o código | Deletar ou atualizar |

---

## Aplicação no projeto

### mod13 — Interfaces

```java
// Anti-padrão removido: static mutable field em interface
// ❌ ANTES: estado compartilhado e mutável na interface
interface UsersRepository {
    ArrayList<User> users = new ArrayList<>();  // mutable static! qualquer um modifica
}

// ✅ DEPOIS: estado encapsulado na implementação
interface UsersRepository {
    Optional<User> findById(int id);
    Optional<User> findByName(String name);
    List<User> findAll();
    void save(User user);
}

class UsersRepositoryInMemory implements UsersRepository {
    private final List<User> users = new ArrayList<>();  // privado, encapsulado
    // ...
}
```

### mod25_projeto02 — Use Cases

```java
// Use Case focado: uma única intenção
class FindCustomerUseCase {
    private final ICustomerRepository repository;

    Optional<Customer> handle(String cpf) {
        return repository.find(cpf);
    }
    // Sem lógica de negócio adicional — simples e coeso
}
```

### mod31 — CourseRecord

```java
// Record imutável como read model — zero mutabilidade, zero carga
record CourseRecord(Long id, String code, String name) {
    CourseRecord {
        Objects.requireNonNull(code, "code required");
        Objects.requireNonNull(name, "name required");
    }
}
// equals, hashCode, toString, getters — todos gerados automaticamente
```

---

## Resumo

| Princípio | Efeito na carga cognitiva |
|---|---|
| Nomes reveladores | Elimina necessidade de comentários |
| Funções pequenas | Cabe na memória de trabalho |
| Guard clauses | Reduz aninhamento mental |
| Imutabilidade | Elimina rastreamento de estado |
| ≤ 3 parâmetros | Dentro do limite de Miller (7±2) |
| Enums vs booleans | Intenção explícita em chamadas |
| Java 21 records/patterns | Menos linhas = menos para ler |

> **Métrica simples:** se você precisa de um comentário para explicar *o que* o código faz,
> o código pode ser simplificado. Comentários devem explicar *por que*, não *o que*.
