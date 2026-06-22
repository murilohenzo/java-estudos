# Mod 19 — Reflection API

> **Conceito central:** Inspecionar e manipular classes, campos, métodos e annotations em runtime.  
> `java.lang.reflect` · Annotation Processing · `@Auditable` · `ClassTable` mapping

---

## Conteúdo

```
mod19/src/
├── reflections/
│   ├── Auditable.java        # Custom annotation @Auditable
│   ├── AuditProcessor.java   # Processa @Auditable via Reflection
│   ├── User.java             # Entidade anotada
│   └── Main.java
└── exercicio/
    └── annotation/
        ├── ClassTable.java   # @ClassTable — mapeia classe para tabela SQL
        ├── Column.java       # @Column — mapeia campo para coluna SQL
        ├── Entity.java       # Entidade anotada
        └── Main.java         # Lê annotations e simula geração de SQL DDL
```

---

## Reflection API — O que é e para que serve

A Reflection API permite ao código Java **inspecionar e manipular sua própria estrutura em runtime**:
- Descobrir campos, métodos e construtores de qualquer classe
- Ler e escrever campos privados
- Invocar métodos por nome
- Instanciar objetos sem saber o tipo em compile time

**Usos reais:**
- Frameworks ORM (Hibernate lê `@Column`, `@Entity`)
- Frameworks DI (Spring inspeciona `@Autowired`, `@Component`)
- Serialização/Desserialização (Jackson lê campos para JSON)
- Testes (JUnit descobre métodos `@Test`)

---

## Annotation Processor — @Auditable

```java
// Annotation de auditoria
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String author() default "system";
    String version() default "1.0";
}

// Entidade anotada
@Auditable(author = "Murilo", version = "2.0")
public class User {
    private String name;
    private String email;
}

// Processador que lê a annotation em runtime
public class AuditProcessor {
    public static void process(Object obj) {
        Class<?> clazz = obj.getClass();

        if (clazz.isAnnotationPresent(Auditable.class)) {
            Auditable audit = clazz.getAnnotation(Auditable.class);
            System.out.printf("[AUDIT] Class: %s | Author: %s | Version: %s%n",
                clazz.getSimpleName(), audit.author(), audit.version());
        }

        // Inspeciona campos
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);  // acessa campos privados
            try {
                System.out.printf("  field: %s = %s%n", field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                // campo inacessível mesmo com setAccessible (módulos Java 9+)
            }
        }
    }
}
```

---

## Mapeamento ORM Simplificado — @ClassTable e @Column

```java
// Annotations de mapeamento
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassTable {
    String name();  // nome da tabela SQL
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name();         // nome da coluna
    boolean nullable() default true;
    int length() default 255;
}

// Entidade anotada
@ClassTable(name = "TB_USER")
public class Entity {
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_name", length = 100, nullable = false)
    private String name;

    @Column(name = "email")
    private String email;
}

// Geração dinâmica de DDL via Reflection
public static String generateDDL(Class<?> clazz) {
    ClassTable table = clazz.getAnnotation(ClassTable.class);
    if (table == null) throw new IllegalArgumentException("@ClassTable required");

    var sb = new StringBuilder("CREATE TABLE " + table.name() + " (\n");

    for (Field field : clazz.getDeclaredFields()) {
        if (!field.isAnnotationPresent(Column.class)) continue;
        Column col = field.getAnnotation(Column.class);

        sb.append("  ").append(col.name());
        sb.append(" ").append(mapJavaToSqlType(field.getType()));

        if (col.length() > 0 && field.getType() == String.class)
            sb.append("(").append(col.length()).append(")");

        if (!col.nullable()) sb.append(" NOT NULL");
        sb.append(",\n");
    }

    return sb.append(");").toString();
}

// Output gerado:
// CREATE TABLE TB_USER (
//   id BIGINT NOT NULL,
//   user_name VARCHAR(100) NOT NULL,
//   email VARCHAR(255),
// );
```

---

## Reflection API — Referência Rápida

```java
Class<?> clazz = MyClass.class;          // ou obj.getClass()

// Campos
Field[] all    = clazz.getDeclaredFields(); // todos (inclui private)
Field[] public_ = clazz.getFields();       // apenas public (inclui herdados)
field.setAccessible(true);                  // permite acesso a private

// Métodos
Method m = clazz.getDeclaredMethod("methodName", String.class, int.class);
m.setAccessible(true);
Object result = m.invoke(instance, "arg1", 42);

// Construtores
Constructor<?> ctor = clazz.getDeclaredConstructor(String.class);
Object obj = ctor.newInstance("value");

// Annotations
clazz.isAnnotationPresent(MyAnnotation.class);  // tem a annotation?
MyAnnotation ann = clazz.getAnnotation(MyAnnotation.class);
```

---

## Cuidados com Reflection

| Risco | Mitigação |
|-------|-----------|
| **Performance** | Reflection é 2–10x mais lento; use cache para `Method`/`Field` objects |
| **Segurança** | `setAccessible(true)` ignora encapsulamento; evite em código não confiável |
| **Manutenção** | Erros em nomes de métodos aparecem em runtime; documente contratos |
| **Java 9+ Modules** | `setAccessible()` pode falhar em módulos não abertos (`--add-opens` necessário) |

**Regra:** use Reflection apenas quando não há alternativa em compile time (frameworks, ferramentas de teste). Em código de aplicação, prefira interfaces e polimorfismo.

---

## Próximo Módulo

[mod20\_21 — Lambda Expressions](../mod20_21/README.md)
