# Mod 14 — Projeto 01: CRUD de Clientes com Swing

> **Primeiro projeto integrado:** aplica Repository Pattern, Use Cases e UI Swing em um CRUD completo.  
> Arquitetura em camadas · Injeção de dependência manual · `JOptionPane` · `HashMap`

---

## Arquitetura

```
mod14_projeto01/src/
├── domain/
│   └── Client.java              # Entidade: id, name, cpf, address, phone
├── repository/
│   ├── IClientsRepository.java  # Contrato de persistência
│   └── implement/
│       ├── ClientMapRepositoryInMemory.java  # HashMap<Long, Client>
│       └── ClientSetRepositoryInMemory.java  # HashSet<Client>
├── useCases/
│   ├── CreateClientUseCase.java
│   ├── FindClientUseCase.java
│   ├── FindAllClientsUseCase.java
│   ├── UpdateClientUseCase.java
│   └── DeleteClientUseCase.java
├── application/
│   └── App.java                 # Orquestração da UI com JOptionPane
├── lib/
│   └── LoggerCtx.java           # Utilitário de log no console
└── Main.java
```

---

## Camadas e Responsabilidades

### `domain/` — Modelo de Negócio
Entidade pura sem dependências de framework. `equals()` e `hashCode()` por CPF (chave de negócio).

```java
public class Client {
    private Long id;
    private String name;
    private String cpf;
    // ...

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Client c)) return false;
        return Objects.equals(cpf, c.cpf);  // igualdade por CPF
    }
}
```

### `repository/` — Acesso a Dados
Interface `IClientsRepository` desacoplada das implementações.
Duas implementações in-memory demonstram diferentes estratégias:

```java
// HashMap — lookup O(1) por ID
public class ClientMapRepositoryInMemory implements IClientsRepository {
    private final Map<Long, Client> clients = new HashMap<>();

    @Override
    public Client save(Client client) {
        clients.put(client.getId(), client);
        return client;
    }

    @Override
    public Optional<Client> findByCpf(String cpf) {
        return clients.values().stream()
            .filter(c -> c.getCpf().equals(cpf))
            .findFirst();
    }
}

// HashSet — garante unicidade por equals/hashCode
public class ClientSetRepositoryInMemory implements IClientsRepository {
    private final Set<Client> clients = new HashSet<>();
}
```

### `useCases/` — Lógica de Aplicação
Cada use case tem uma única responsabilidade (Single Responsibility Principle).
Dependência injetada via construtor (Dependency Injection sem framework).

```java
public class CreateClientUseCase {
    private final IClientsRepository repository;

    public CreateClientUseCase(IClientsRepository repository) {
        this.repository = repository;  // DI via construtor
    }

    public Client execute(Client client) {
        if (repository.findByCpf(client.getCpf()).isPresent()) {
            throw new ClientAlreadyExistsException("CPF já cadastrado: " + client.getCpf());
        }
        return repository.save(client);
    }
}
```

### `application/` — Interface com Usuário
`App.java` usa `JOptionPane` como UI e orquestra os use cases.
A UI não conhece o repositório — apenas os use cases.

```java
public class App {
    private final IClientsRepository repository = new ClientMapRepositoryInMemory();

    // Use cases instanciados com o mesmo repositório
    private final CreateClientUseCase createUseCase = new CreateClientUseCase(repository);
    private final FindClientUseCase   findUseCase   = new FindClientUseCase(repository);
    // ...

    public void run() {
        // Menu principal com JOptionPane
        String option = JOptionPane.showInputDialog(
            "1 - Cadastrar\n2 - Buscar\n3 - Listar\n4 - Atualizar\n5 - Deletar\n0 - Sair"
        );
        // ...
    }
}
```

---

## Fluxo de Execução

```
Main.java
   └── new App().run()
         ├── JOptionPane (menu)
         ├── CreateClientUseCase.execute(client)
         │     └── IClientsRepository.save(client)
         │           └── ClientMapRepositoryInMemory.save(client)  ← HashMap
         └── FindClientUseCase.execute(cpf)
               └── IClientsRepository.findByCpf(cpf)
```

---

## Princípios SOLID aplicados

| Princípio | Onde |
|-----------|------|
| **S** — Single Responsibility | Cada UseCase faz uma coisa só |
| **O** — Open/Closed | Novos repositórios sem alterar use cases |
| **L** — Liskov Substitution | `ClientSetRepositoryInMemory` substitui `ClientMapRepositoryInMemory` |
| **I** — Interface Segregation | `IClientsRepository` — contrato coeso |
| **D** — Dependency Inversion | Use cases dependem de `IClientsRepository`, não de implementações |

---

## Como Executar

Abra `mod14_projeto01` no IntelliJ IDEA e execute `Main.java`.
A interface Swing abrirá com o menu de opções.

---

## Próximo Módulo

[mod15\_mod16 — Design Patterns GOF](../mod15_mod16/README.md)
