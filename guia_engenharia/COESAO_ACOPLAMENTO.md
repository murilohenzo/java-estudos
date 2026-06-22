# Alta Coesão e Baixo Acoplamento

> **"Um módulo deve ter uma, e apenas uma, razão para mudar."** — SRP, Robert C. Martin  
> **"Programe para interfaces, não implementações."** — GoF

---

## Índice

- [Definições fundamentais](#definições-fundamentais)
- [Coesão — o que é e como medir](#coesão--o-que-é-e-como-medir)
- [Acoplamento — tipos e impactos](#acoplamento--tipos-e-impactos)
- [SOLID como guia prático](#solid-como-guia-prático)
- [Padrões para baixo acoplamento](#padrões-para-baixo-acoplamento)
- [Composição vs Herança](#composição-vs-herança)
- [Pacotes — coesão em nível macro](#pacotes--coesão-em-nível-macro)
- [Aplicação no projeto](#aplicação-no-projeto)
- [Métricas objetivas](#métricas-objetivas)

---

## Definições fundamentais

### Alta Coesão

> Uma classe/módulo tem **alta coesão** quando todos os seus elementos trabalham juntos
> para uma **única responsabilidade bem definida**.

Analogia: um bisturi cirúrgico tem alta coesão — faz uma coisa com precisão.  
Um canivete suíço tem baixa coesão — faz muitas coisas, nenhuma perfeitamente.

### Baixo Acoplamento

> Um módulo tem **baixo acoplamento** quando depende do **mínimo possível** de outros módulos,
> e quando essas dependências são com **abstrações** (interfaces), não implementações concretas.

Analogia: uma tomada tem baixo acoplamento com o aparelho — qualquer aparelho com o plugue correto funciona.

### A relação entre eles

```
Alta Coesão + Baixo Acoplamento
        ↓
Classes pequenas e focadas que colaboram por interfaces
        ↓
Mudanças ficam localizadas — um bug/feature afeta poucos arquivos
        ↓
Testabilidade, manutenibilidade e extensibilidade elevadas
```

---

## Coesão — o que é e como medir

### Tipos de coesão (do pior ao melhor)

| Nível | Descrição | Exemplo |
|---|---|---|
| **Coincidental** | Funções agrupadas sem relação | `Utils.java` com 50 métodos estáticos |
| **Lógica** | Funções relacionadas por tipo genérico | `StringUtils`, `DateHelper` |
| **Temporal** | Funções chamadas juntas (ex: inicialização) | `init()` faz tudo |
| **Procedural** | Funções em sequência fixa | Pipeline rígido |
| **Comunicacional** | Funções que operam sobre os mesmos dados | Melhor |
| **Funcional** | Cada função contribui para uma única tarefa | **Ideal** |

### God Class — o anti-padrão de baixa coesão

```java
// ❌ CustomerService faz tudo — baixíssima coesão
class CustomerService {
    void register(Customer c)       { /* valida, persiste, envia email */ }
    void sendWelcomeEmail(Customer c) { /* lógica de e-mail */ }
    void generateReport()            { /* gera PDF */ }
    void exportToCsv()               { /* exporta CSV */ }
    void calculateLoyaltyPoints()    { /* regra de pontos */ }
    void syncWithCRM()               { /* integração externa */ }
    double calculateDiscount()       { /* regra de desconto */ }
    boolean validateCpf(String cpf)  { /* validação */ }
}
```

```java
// ✅ Responsabilidades separadas — alta coesão
class CustomerRegistrationService {
    void register(Customer c) { validate(c); repository.save(c); eventBus.publish(new CustomerRegistered(c)); }
}

class CustomerNotificationService {
    void sendWelcomeEmail(Customer c) { emailClient.send(welcomeTemplate.render(c)); }
}

class LoyaltyService {
    int calculatePoints(Customer c) { return loyaltyPolicy.calculate(c.orders()); }
}

class CpfValidator {
    boolean isValid(String cpf) { /* algoritmo de validação */ }
}
```

---

## Acoplamento — tipos e impactos

### Tipos de acoplamento (do pior ao melhor)

| Tipo | Descrição | Exemplo |
|---|---|---|
| **Content** | Acessa dados internos de outro módulo | `other.internalField` |
| **Common** | Compartilha variável global | `static mutable field` |
| **Control** | Passa flag que controla comportamento interno | `process(order, true)` |
| **Stamp** | Passa estrutura completa quando só precisa de parte | `calculateAge(User user)` quando só precisa da data |
| **Data** | Passa apenas os dados necessários | `calculateAge(LocalDate birthDate)` |
| **Message** | Chama método sem passar dados (Tell, Don't Ask) | `customer.upgrade()` |

### Acoplamento concreto vs abstrato

```java
// ❌ Acoplamento a implementação concreta
class OrderService {
    private final MySqlOrderRepository repository;  // acoplado a MySQL

    OrderService() {
        this.repository = new MySqlOrderRepository();  // instanciação direta — hardcoded
    }
}

// ✅ Acoplamento a abstração (interface)
class OrderService {
    private final OrderRepository repository;  // acoplado apenas à interface

    OrderService(OrderRepository repository) {  // injeção de dependência
        this.repository = repository;
    }
    // Pode receber MySqlOrderRepository, InMemoryOrderRepository, MockOrderRepository...
}
```

---

## SOLID como guia prático

### S — Single Responsibility Principle

> Uma classe deve ter **uma única razão para mudar**.

```java
// ❌ Duas razões para mudar: regra de negócio E formato de saída
class Invoice {
    double calculate() { ... }        // regra de negócio
    String printHtml() { ... }        // formatação HTML
    void saveToDatabase() { ... }     // persistência
}

// ✅ Três responsabilidades, três classes
class Invoice        { double calculate() { ... } }
class InvoiceHtmlRenderer { String render(Invoice i) { ... } }
class InvoiceRepository   { void save(Invoice i) { ... } }
```

### O — Open/Closed Principle

> Aberto para extensão, **fechado para modificação**.

```java
// ❌ Para adicionar novo desconto, modificar o método existente
double calculateDiscount(Customer c) {
    if (c.type() == CustomerType.PREMIUM)  return 0.20;
    if (c.type() == CustomerType.VIP)      return 0.30;
    // ← toda nova regra exige modificar este método
    return 0.0;
}

// ✅ Extensão por novas implementações sem tocar no código existente
interface DiscountPolicy {
    double calculate(Customer customer);
}

record PremiumDiscount()  implements DiscountPolicy {
    public double calculate(Customer c) { return c.isPremium() ? 0.20 : 0.0; }
}

record SeasonalDiscount(double rate) implements DiscountPolicy {
    public double calculate(Customer c) { return rate; }
}

// Nova política: cria nova classe, não modifica as existentes
record LoyaltyDiscount(int minOrders) implements DiscountPolicy {
    public double calculate(Customer c) {
        return c.totalOrders() >= minOrders ? 0.15 : 0.0;
    }
}
```

### L — Liskov Substitution Principle

> Subtipos devem ser **substituíveis** pelos seus tipos base sem alterar o comportamento correto.

```java
// ❌ Viola LSP — retângulo e quadrado têm invariantes incompatíveis
class Rectangle {
    int width, height;
    void setWidth(int w)  { this.width = w; }
    void setHeight(int h) { this.height = h; }
    int area() { return width * height; }
}

class Square extends Rectangle {
    @Override
    void setWidth(int w)  { this.width = this.height = w; }   // quebra invariante de Rectangle!
    @Override
    void setHeight(int h) { this.width = this.height = h; }
}

// ✅ LSP respeitado — sealed hierarchy com records imutáveis
sealed interface Shape permits Rectangle, Square {
    int area();
}
record Rectangle(int width, int height) implements Shape {
    public int area() { return width * height; }
}
record Square(int side) implements Shape {
    public int area() { return side * side; }
}
```

### I — Interface Segregation Principle

> Clientes não devem ser forçados a depender de **interfaces que não usam**.

```java
// ❌ Interface "gorda" — implementações são forçadas a implementar tudo
interface Animal {
    void eat();
    void sleep();
    void fly();    // ← patos voam, cachorros não
    void swim();   // ← patos nadam, pássaros não
    void bark();   // ← só cachorros latem
}

// ✅ Interfaces segregadas — implementações pegam só o que precisam
interface Eater   { void eat(); }
interface Sleeper { void sleep(); }
interface Flyer   { void fly(); }
interface Swimmer { void swim(); }

class Duck        implements Eater, Sleeper, Flyer, Swimmer { ... }
class Dog         implements Eater, Sleeper { void bark() { ... } }
class SwimmingFish implements Eater, Swimmer { ... }
```

### D — Dependency Inversion Principle

> Módulos de alto nível não devem depender de módulos de baixo nível.  
> Ambos devem depender de **abstrações**.

```java
// ❌ Alto nível (UseCase) depende de baixo nível (concreto)
class RegisterCourseUseCase {
    private final CourseJpaRepository repository;  // concreto!

    // Impossível testar sem banco de dados
    void handle(CreateCourseCommand cmd) {
        repository.save(new Course(cmd.code(), cmd.name()));
    }
}

// ✅ DIP — alto nível depende de abstração
class RegisterCourseUseCase {
    private final ICourseRepository repository;  // abstração!

    void handle(CreateCourseCommand cmd) {
        repository.save(new Course(cmd.code(), cmd.name()));
    }
}

// Testes: inject InMemoryCourseRepository (sem DB)
// Produção: inject CourseJpaRepository (PostgreSQL)
// Migração: inject CourseJdbiRepository (JDBI)
```

---

## Padrões para baixo acoplamento

### 1. Dependency Injection (DI)

```java
// ❌ new() dentro do código — acoplamento de instanciação
class OrderService {
    private final OrderRepository repo = new MySqlOrderRepository();
}

// ✅ Constructor injection — dependência vem de fora
class OrderService {
    private final OrderRepository repo;

    OrderService(OrderRepository repo) { this.repo = repo; }
}

// Composição raiz (main ou factory) — único lugar que conhece os concretos
var service = new OrderService(new MySqlOrderRepository(dataSource));
```

### 2. Repository Pattern — separa domínio da infraestrutura

```java
// Domínio (alta coesão, zero acoplamento a DB)
class Course {
    private final String code;
    private final String name;
    // regras de negócio aqui
}

// Abstração (contrato)
interface ICourseRepository {
    Optional<Course> findByCode(String code);
    void save(Course course);
}

// Infraestrutura (pode ser trocada independentemente)
class CourseJpaRepository  implements ICourseRepository { ... }
class CourseJdbiRepository implements ICourseRepository { ... }
class InMemoryCourseRepository implements ICourseRepository { ... }
```

### 3. Event-Driven — desacopla produtor do consumidor

```java
// ❌ Acoplamento direto: OrderService conhece todos os listeners
class OrderService {
    private final EmailService emailService;
    private final InventoryService inventoryService;
    private final AuditService auditService;

    void placeOrder(Order order) {
        repository.save(order);
        emailService.sendConfirmation(order);      // acoplado!
        inventoryService.decrement(order.items()); // acoplado!
        auditService.log(order);                   // acoplado!
    }
}

// ✅ Evento — OrderService não conhece ninguém além do repositório
class OrderService {
    private final OrderRepository repository;
    private final EventBus eventBus;

    void placeOrder(Order order) {
        repository.save(order);
        eventBus.publish(new OrderPlaced(order));  // desacoplado — qualquer listener registrado reage
    }
}

// Listeners independentes — cada um faz uma coisa
class EmailNotifier    implements EventListener<OrderPlaced> { ... }
class InventoryUpdater implements EventListener<OrderPlaced> { ... }
class AuditLogger      implements EventListener<OrderPlaced> { ... }
```

### 4. Strategy Pattern — algoritmos intercambiáveis

```java
// ❌ if/else para cada variante — modifica o código sempre que muda
double ship(Order order) {
    if (shippingType == ShippingType.EXPRESS) return order.weight() * 5.0;
    if (shippingType == ShippingType.STANDARD) return order.weight() * 2.0;
    throw new IllegalArgumentException();
}

// ✅ Strategy — nova estratégia = nova classe, sem modificar o existente
interface ShippingStrategy {
    double calculate(Order order);
}

record ExpressShipping()  implements ShippingStrategy {
    public double calculate(Order o) { return o.weight() * 5.0; }
}

record StandardShipping() implements ShippingStrategy {
    public double calculate(Order o) { return o.weight() * 2.0; }
}

// Drone shipping amanhã? Só criar DroneShipping — sem tocar nos existentes
```

---

## Composição vs Herança

> **Prefira composição à herança** — GoF, Item 18 do Effective Java.

Herança cria o acoplamento mais forte possível: a subclasse depende de **todos**
os detalhes de implementação da superclasse.

```java
// ❌ Herança para reutilizar comportamento — frágil
class InstrumentedHashSet<E> extends HashSet<E> {
    private int addCount = 0;

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);  // HashSet.addAll chama add() — dobra o count!
    }
}

// ✅ Composição — wrapping sem dependência de implementação
class InstrumentedSet<E> implements Set<E> {
    private final Set<E> delegate;   // compõe, não herda
    private int addCount = 0;

    InstrumentedSet(Set<E> delegate) { this.delegate = delegate; }

    @Override
    public boolean add(E e) { addCount++; return delegate.add(e); }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return delegate.addAll(c);  // sem surpresas — delega direto
    }
    // ... outros métodos delegam
}
```

### Quando herança é apropriada

Herança faz sentido quando há uma relação genuína **"é um"** e a hierarquia é:
- Sealed (o compilador controla os subtipos)
- Shallow (máx 2-3 níveis)
- Estável (a superclasse não muda frequentemente)

```java
// ✅ Herança legítima — hierarquia sealed, comportamento polimórfico claro
sealed abstract class Shape permits Circle, Rectangle, Triangle {
    abstract double area();
    double perimeter() { ... }  // implementação base compartilhada
}
```

---

## Pacotes — coesão em nível macro

**Package by feature** (alta coesão) vs **package by layer** (baixa coesão):

```
// ❌ Package by layer — baixa coesão dentro de cada pacote
br.com.murilo
  ├── repository/     ← Course, Customer, Order misturados
  ├── service/        ← lógicas totalmente diferentes juntas
  ├── domain/         ← entidades sem relação agrupadas
  └── controller/     ← idem

// ✅ Package by feature — alta coesão, baixo acoplamento entre pacotes
br.com.murilo
  ├── course/
  │     ├── Course.java           (domain)
  │     ├── ICourseRepository.java (port)
  │     ├── CourseJpaRepository.java (adapter)
  │     └── RegisterCourseUseCase.java (use case)
  ├── registration/
  │     ├── Registration.java
  │     ├── RegisterStudentUseCase.java
  │     └── IRegistrationRepository.java
  └── student/
        └── ...
```

**Benefícios do package by feature:**
- Deletar um feature = deletar um pacote (sem arquivos espalhados)
- Visibility: classes `package-private` ficam invisíveis fora do feature
- Onboarding: o novo dev entende cada feature de forma isolada

---

## Aplicação no projeto

### Evolução arquitetural observada nos módulos

| Módulo | Padrão | Coesão | Acoplamento |
|---|---|---|---|
| `mod13` | Interfaces + Repository | Alta (após fix) | Baixo |
| `mod14_projeto01` | Layered + Use Case | Média | Médio |
| `mod24` | Service + DAO + Test Doubles | Alta | Baixo |
| `mod25_projeto02` | Clean Use Case | Alta | Muito baixo |
| `mod31` | Repository + 3 adapters | Alta | Muito baixo (por interface) |

### mod31 — ICourseRepository como Port (DIP aplicado)

```java
// Port — definido no domínio, desacoplado de infraestrutura
interface ICourseRepository {
    Optional<Course> findByCode(String code);
    void save(Course course);
    List<Course> findAll();
}

// Adapters — 3 implementações intercambiáveis
class CourseJpaRepository      implements ICourseRepository { ... }
class CourseHibernateRepository implements ICourseRepository { ... }
class CourseJdbiRepository     implements ICourseRepository { ... }

// Use case — depende apenas do Port
class RegisterCourseUseCase {
    private final ICourseRepository repository;  // qual adapter? não importa para o use case
    // ...
}
```

### mod25_projeto02 — resultado das boas práticas

```java
// Antes: CPF como Long — dados de domínio em tipo errado
// Depois: String com validação no constructor

// Antes: find() com stream().filter() = O(n)
// Depois: map.get(cpf) = O(1) — structure correta para o acesso esperado

// Antes: sem Use Cases — lógica espalhada
// Depois: FindCustomerUseCase, DeleteCustomerUseCase, etc. — alta coesão
```

---

## Métricas objetivas

| Métrica | Verde | Amarelo | Vermelho |
|---|---|---|---|
| Linhas por classe | ≤ 200 | 200–500 | > 500 |
| Métodos por classe | ≤ 10 | 10–20 | > 20 |
| Parâmetros por método | ≤ 3 | 4–5 | > 5 |
| Profundidade de herança | ≤ 2 | 3–4 | > 4 |
| Dependências de uma classe | ≤ 5 | 5–10 | > 10 |
| Cyclomatic complexity | ≤ 5 | 5–10 | > 10 |

---

## Resumo

```
Alta Coesão                          Baixo Acoplamento
────────────────────────────────     ──────────────────────────────────
Uma classe = uma responsabilidade    Dependa de interfaces, não concretos
Métodos relacionados ao mesmo dado   Injete dependências (não instancie)
Package by feature                   Event-driven para listeners múltiplos
Extrair God Classes                  Composição > Herança
Records para dados imutáveis         Strategy para variações de algoritmo
```

**A regra de ouro:** quando uma mudança de requisito faz você modificar **muitos arquivos
não relacionados**, há acoplamento excessivo. Quando faz você modificar **uma classe
grande em múltiplos lugares**, há baixa coesão.
