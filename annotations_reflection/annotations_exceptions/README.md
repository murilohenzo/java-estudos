# Mod 18 — Annotations e Exceptions

> **Conceito central:** Metadados em código e tratamento estruturado de erros.  
> Custom Annotations · `@Retention` · `@Target` · Checked vs Unchecked Exceptions · Exception hierarchy

---

## Conteúdo

```
mod18/src/br/com/murilo/
├── annotation/
│   ├── Anotacao.java         # Custom annotation @Anotacao
│   └── ExemploAnnotation.java
├── exceptions/
│   ├── DomainException.java       # Exceção de domínio base (checked)
│   ├── UserNotFoundException.java # extends DomainException
│   ├── UserService.java           # Serviço que lança exceções tipadas
│   └── Main.java
└── exercicio/
    └── Main.java              # Aplicação das duas técnicas
```

---

## Custom Annotations

### Criando uma Annotation

```java
// Meta-anotações definem o comportamento da annotation
@Documented          // aparece no Javadoc
@Target(ElementType.FIELD)     // só pode ser usada em campos
@Retention(RetentionPolicy.RUNTIME)  // disponível em runtime via Reflection
public @interface Anotacao {

    // Elementos da annotation (com valores padrão)
    String value() default "";
    boolean required() default true;
    int order() default 0;
}
```

### Meta-Anotações

| Meta-Anotação | `@Retention` valores | Significado |
|---|---|---|
| `@Retention` | `SOURCE` | Apenas no código-fonte; descartada pelo compilador |
| `@Retention` | `CLASS` | No bytecode; indisponível em runtime (padrão) |
| `@Retention` | `RUNTIME` | No bytecode E disponível em runtime via Reflection |
| `@Target` | `FIELD`, `METHOD`, `TYPE`, `PARAMETER`, `CONSTRUCTOR` | Onde a annotation pode ser aplicada |
| `@Documented` | — | Inclui no Javadoc gerado |
| `@Inherited` | — | Subclasses herdam a annotation da superclasse |

### Usando a Annotation

```java
public class User {
    @Anotacao(value = "nome completo", required = true, order = 1)
    private String name;

    @Anotacao(value = "cpf", required = true, order = 2)
    private String cpf;

    @Anotacao(required = false)
    private String nickname;
}
```

### Lendo Annotations em Runtime (Reflection)

```java
// Lendo annotation de um campo
Field[] fields = User.class.getDeclaredFields();
for (Field field : fields) {
    if (field.isAnnotationPresent(Anotacao.class)) {
        Anotacao ann = field.getAnnotation(Anotacao.class);
        System.out.printf("Campo: %s | value: %s | required: %b%n",
            field.getName(), ann.value(), ann.required());
    }
}
```

---

## Exception Handling

### Hierarquia de Exceções Java

```
Throwable
├── Error          (não capturar — problemas da JVM: OutOfMemoryError, StackOverflowError)
└── Exception
    ├── RuntimeException  → UNCHECKED — não precisa declarar no throws
    │   ├── NullPointerException
    │   ├── IllegalArgumentException
    │   ├── IndexOutOfBoundsException
    │   └── ClassCastException
    └── IOException       → CHECKED — deve ser declarada ou capturada
        ├── FileNotFoundException
        └── ...

// Exceções de domínio (este projeto)
Exception
└── DomainException (checked — requer tratamento explícito)
    └── UserNotFoundException
```

### Checked vs Unchecked

| | Checked | Unchecked |
|---|---|---|
| Herda de | `Exception` | `RuntimeException` |
| Declarar no `throws`? | **Obrigatório** | Opcional |
| Quando usar | Erros recuperáveis esperados (arquivo não encontrado, usuário inválido) | Erros de programação (null, índice inválido) |
| Exemplo no projeto | `UserNotFoundException`, `CustomerNotFoundException` | `IllegalArgumentException` |

```java
// Exceção de domínio checked
public class DomainException extends Exception {
    public DomainException(String message)                 { super(message); }
    public DomainException(String message, Throwable cause){ super(message, cause); }
}

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(String cpf) {
        super("Usuário não encontrado com CPF: " + cpf);
    }
}

// Service lança exceção de domínio
public class UserService {
    private final UsersRepository repository;

    public User findByCpf(String cpf) throws UserNotFoundException {
        return repository.findByCpf(cpf)
            .orElseThrow(() -> new UserNotFoundException(cpf));
    }
}

// Tratamento no caller
try {
    User user = service.findByCpf("00000000000");
} catch (UserNotFoundException e) {
    System.err.println("Erro: " + e.getMessage());
    // loga e retorna resposta 404 em REST
}
```

### Best Practices

```java
// ❌ Nunca silenciar exceções
catch (Exception e) { /* vazio */ }

// ❌ Nunca capturar Exception genérica desnecessariamente
catch (Exception e) { e.printStackTrace(); }

// ✅ Capturar o tipo mais específico
catch (UserNotFoundException e) { /* tratar usuário não encontrado */ }
catch (DatabaseException e)      { /* tratar falha de banco */ }

// ✅ Try-with-resources para recursos que implementam AutoCloseable
try (var conn = dataSource.getConnection()) {
    // conn.close() chamado automaticamente mesmo em exceção
}

// ✅ Exceção com causa (exception chaining)
catch (SQLException e) {
    throw new DataAccessException("Falha ao buscar usuário", e);
}
```

---

## Próximo Módulo

[mod19 — Reflection API](../mod19/README.md)
