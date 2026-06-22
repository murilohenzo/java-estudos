# Mod 25 v01 — Generic DAO com Reflection e Singleton Map

> **Abordagem alternativa** ao Projeto 02: Generic DAO com Reflection para extrair chave primária.  
> `@TipoChave` · `GenericDAO<T,E>` · `GenericService<T,E>` · `SingletonMap` · Reflection API

---

## Visão Geral

Versão alternativa do mesmo domínio (Cliente CRUD) com uma arquitetura mais genérica:
- Annotation `@TipoChave` marca qual campo/método retorna a chave primária
- `GenericDAO` usa Reflection para extrair a chave em runtime, sem casting
- `SingletonMap` simula um banco de dados em memória compartilhado entre DAOs
- `GenericService` delega todas as operações para `IGenericDAO`

---

## Estrutura

```
mod25_projeto02_v01/src/main/java/
├── anotacao/
│   └── TipoChave.java                  # @TipoChave("nomeDoGetter")
└── br/com/murilo/
    ├── domain/
    │   └── Cliente.java               # @TipoChave("getCpf") no campo cpf
    ├── dao/
    │   ├── Persistente.java           # Interface marcadora (sem métodos)
    │   ├── IClienteDAO.java           # extends IGenericDAO<Cliente, Long>
    │   ├── ClienteDAO.java            # extends GenericDAO<Cliente, Long>
    │   └── generic/
    │       ├── IGenericDAO.java       # Interface CRUD genérica
    │       ├── GenericDAO.java        # Implementação com Reflection + SingletonMap
    │       └── SingletonMap.java      # Map<Class, Map<?, ?>> singleton
    ├── services/
    │   ├── IClienteService.java       # extends IGenericService<Cliente, Long>
    │   ├── ClienteService.java        # extends GenericService + buscarPorCPF
    │   └── generic/
    │       ├── IGenericService.java   # Interface service genérica
    │       └── GenericService.java    # Delega para IGenericDAO
    └── exceptions/
        └── TipoChaveNaoEncontradaException.java

src/test/java/br/com/murilo/
├── AllTests.java          # Suite JUnit 4
├── ClienteDAOTest.java    # Testa GenericDAO diretamente
├── ClienteServiceTest.java# Testa ClienteService com Mock
└── dao/
    └── ClienteDaoMock.java # Test Double (Fake)
```

---

## Annotation @TipoChave

```java
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TipoChave {
    String value();  // nome do método getter que retorna a chave
}

// Uso na entidade
public class Cliente implements Persistente {
    @TipoChave("getCpf")   // Reflection chamará cliente.getCpf()
    private Long cpf;
    private String nome;
    // ...
}
```

---

## GenericDAO — Extração da Chave via Reflection

```java
public abstract class GenericDAO<T extends Persistente, E extends Serializable>
        implements IGenericDAO<T, E> {

    private final SingletonMap singletonMap;

    // Template Method — subclasse informa o tipo
    public abstract Class<T> getTipoClasse();

    // Template Method — subclasse define como atualizar campos
    public abstract void atualiarDados(T source, T target);

    /**
     * Usa Reflection para encontrar o campo com @TipoChave e invocar seu getter.
     * Permite que GenericDAO funcione com QUALQUER entidade sem cast hardcoded.
     */
    @SuppressWarnings("unchecked")
    public E getChave(T entity) throws TipoChaveNaoEncontradaException {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(TipoChave.class)) continue;

            String methodName = field.getAnnotation(TipoChave.class).value();
            try {
                Method method = entity.getClass().getMethod(methodName);
                return (E) method.invoke(entity);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new TipoChaveNaoEncontradaException(
                    "Getter '" + methodName + "' não encontrado em " + entity.getClass(), e);
            }
        }
        throw new TipoChaveNaoEncontradaException("@TipoChave não encontrado em " + entity.getClass());
    }

    @Override
    public Boolean cadastrar(T entity) throws TipoChaveNaoEncontradaException {
        Map<E, T> mapa = getMapa();
        E chave = getChave(entity);
        if (mapa.containsKey(chave)) return false;  // CPF já existe
        mapa.put(chave, entity);
        return true;
    }
}
```

---

## SingletonMap — Banco de dados em memória

```java
/**
 * Map<Class, Map<Chave, Entidade>> — cada tipo de entidade tem seu próprio mapa.
 * Singleton garante que todos os DAOs compartilham o mesmo "banco".
 */
public class SingletonMap {
    private static SingletonMap instance;
    protected Map<Class, Map<?, ?>> map = new HashMap<>();

    // Bill Pugh Singleton — thread-safe sem synchronized
    public static SingletonMap getInstance() {
        if (instance == null) instance = new SingletonMap();
        return instance;
    }

    public Map<Class, Map<?, ?>> getMap() { return map; }
}

// Acesso interno no GenericDAO:
private Map<E, T> getMapa() {
    Map<E, T> internal = (Map<E, T>) singletonMap.getMap().get(getTipoClasse());
    if (internal == null) {
        internal = new HashMap<>();
        singletonMap.getMap().put(getTipoClasse(), internal);
    }
    return internal;
}
```

---

## ClienteDAO — Implementação específica

```java
public class ClienteDAO extends GenericDAO<Cliente, Long> implements IClienteDAO {

    @Override
    public Class<Cliente> getTipoClasse() { return Cliente.class; }

    @Override
    public void atualiarDados(Cliente source, Cliente target) {
        target.setNome(source.getNome());
        target.setCpf(source.getCpf());
        target.setTel(source.getTel());
        target.setEnd(source.getEnd());
        target.setEstado(source.getEstado());
        target.setCidade(source.getCidade());
        target.setNumero(source.getNumero());
    }
    // Herda cadastrar, excluir, alterar, consultar, buscarTodos de GenericDAO
}
```

---

## ClienteService com buscarPorCPF

```java
public class ClienteService extends GenericService<Cliente, Long> implements IClienteService {

    public ClienteService(IGenericDAO<Cliente, Long> dao) { super(dao); }

    // Método específico além do CRUD genérico
    @Override
    public Cliente buscarPorCPF(Long cpf) {
        return this.dao.consultar(cpf);
    }
}
```

---

## Comparativo: v01 (Reflection) vs v02 (Use Cases)

| Aspecto | v01 (Generic DAO + Reflection) | v02 (Use Cases) |
|---------|--------------------------------|-----------------|
| Reutilização | Alta — 1 GenericDAO para N entidades | Baixa — 1 UseCase por operação |
| Legibilidade | Média — Reflection dificulta debug | Alta — lógica explícita |
| Testabilidade | Média — precisa de Mock DAO | Alta — DI via construtor simples |
| Boilerplate | Baixo por entidade nova | Alto por entidade nova |
| Complexidade | Alta (Reflection, Singleton, Generics) | Baixa |
| Uso real | Frameworks (Spring Data) | Aplicações de negócio |

**Conclusão:** v01 demonstra como frameworks como Spring Data JPA implementam repositórios genéricos internamente. v02 é a abordagem recomendada para código de aplicação.

---

## Próximo Módulo

[mod26\_27\_28 — SQL: Fundamentos e Avançado](../mod26_27_28/README.md)
