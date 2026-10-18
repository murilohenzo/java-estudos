# 04 — CDI: a cola que conecta as classes Java

[← Índice](README.md) · [← JSF](03-jsf-primefaces.md) · [EJB →](05-ejb-transacoes.md)

## Antes de começar: CDI não é EJB e não significa transação

**CDI** oferece descoberta, dependências, contextos e interceptores. **EJB** é outro modelo de componente do contêiner. Para um service de negócio comum, `@ApplicationScoped` pode ser suficiente se não há operação atômica a demarcar.

Quando precisa de transação, a escolha mais direta **sem EJB** é `jakarta.transaction.Transactional` no caso de uso gerenciado CDI:

```java
@ApplicationScoped
public class CadastroService {
    @Transactional(Transactional.TxType.REQUIRED)
    public void cadastrar(NovoCliente dados) {
        // Chama repositórios: mesmo contexto T1
    }
}
```

**No laboratório**, a transação é definida de outra maneira: a fachada `@Stateless` EJB usa CMT `REQUIRED` e chama um service CDI **sem `@Transactional`**. Os dois desenhos são válidos; não os junte automaticamente.

Leia a [decisão CDI × EJB × JTA](11-cdi-ejb-transacoes-sem-confusao.md) antes dos detalhes de `@Inject`.

## 1. Por que injeção de dependência?

**Explicação simples:** imagine que `ClienteService` precisa salvar um cliente. Ele não deveria criar manualmente um objeto especializado em banco de dados; deve receber alguém capaz de **salvar clientes**, por meio de uma interface.

Sem inversão de dependência:
```java
class ClienteService {
    private final JpaClienteRepository repo = new JpaClienteRepository();
}
```

Isso acopla o serviço à implementação. Com **CDI**:
```java
@ApplicationScoped
class ClienteService {
    private final ClienteRepository repo;
    protected ClienteService() { this.repo = null; } // proxy CDI

    @Inject
    ClienteService(ClienteRepository repo) {
        this.repo = repo;
    }
}
```

O contêiner resolve o bean; a regra de negócio usa o **contrato** `ClienteRepository`.

Comparativo Spring:
```java
@Service
class ClienteService {
    private final ClienteRepository repo;
    ClienteService(ClienteRepository repo) { this.repo = repo; }
}
```

Em Spring com um único construtor, a anotação `@Autowired` pode ser omitida. Em CDI é recomendável usar `@Inject` para identificar o construtor. O princípio de design é semelhante, mas são contêineres diferentes.

## 2. IoC, DI, CDI e DIP são conceitos diferentes

- **IoC (Inversion of Control):** objetos não comandam todo o processo de construção e ciclo de vida; o framework/contêiner assume o controle de certos pontos.
- **DI (Dependency Injection):** técnica de receber dependências do exterior.
- **CDI (Contexts and Dependency Injection):** especificação com resolução, contextualização, eventos, producers e interceptores.
- **DIP (Dependency Inversion Principle):** regra de arquitetura: alto nível e baixo nível devem depender de abstrações apropriadas.

É possível usar DI por construtor sem seguir DIP (por exemplo, injetando diretamente `JpaClienteRepository`). Para DIP, injete a **interface** correta.

## 3. Ciclo de vida e escopos

```java
@ApplicationScoped
class CalculadoraTaxas {
    public BigDecimal taxaPadrao() { return new BigDecimal("0.02"); }
}
```

`@ApplicationScoped` cria um bean contextual compartilhado no contexto da aplicação. CDI normalmente usa um **client proxy** para scopes normais. **Não confunda com Singleton GoF** ou com `@Singleton` EJB.

Em **Spring**, o escopo padrão é `singleton` por ApplicationContext; o CDI `@Dependent` é o escopo padrão de beans sem scope explícito (nas condições de descoberta aplicáveis). A nomenclatura e o ciclo de vida **não são equivalentes**.

## 4. Como o CDI sabe qual implementação escolher?

```java
public interface Notificador {
    void informar(String mensagem);
}
```

```java
@Qualifier
@Retention(RUNTIME)
@Target({TYPE, FIELD, PARAMETER, METHOD})
public @interface Operacional {}
```

```java
@Operacional
@ApplicationScoped
class NotificadorConsole implements Notificador {
    public void informar(String texto) { System.out.println(texto); }
}
```

```java
@Inject
public ClienteService(@Operacional Notificador notificador) {
    this.notificador = notificador;
}
```

**Por que o qualifier é necessário?** Quando há mais de uma implementação elegível do mesmo contrato, o CDI pode falhar por ambiguidade (`WELD-001409`). O qualifier define a escolha sem depender do nome de uma classe concreta.

**Spring equivalente:** `@Qualifier("notificadorConsole")` ou uma anotação qualifier customizada. **Não copie automaticamente regras de resolução do Spring para o CDI.**

## 5. Constructor injection x proxy x recursos do servidor

O construtor com `@Inject` é preferível no **núcleo de aplicação** porque deixa dependências explícitas e facilita testes com mocks.

Alguns componentes exigem regras adicionais:
- **JPA Entity:** precisa de construtor sem argumentos com visibilidade adequada.
- **EJB Session Bean:** contêiner instancia usando construtor público sem argumentos; dependências são tradicionalmente injetadas por `@EJB` ou `@Inject` em pontos gerenciados.
- **Bean de escopo normal:** precisa ser proxyável; construtores sem argumentos apropriados podem fazer parte do projeto.
- **EntityManager `@PersistenceContext`:** recurso especial fornecido pelo contêiner; pode ser exposto a outros beans por `@Produces`.

Assim, **não é correto afirmar que 100% das classes do Java EE devem ter somente construtor parametrizado**.

## 6. `@Produces`: quando a dependência não é um bean comum

```java
@Dependent
class PersistenceResources {
    @Produces
    @PersistenceContext(unitName = "estudosPU")
    private EntityManager entityManager;
}
```

O produtor oferece um `EntityManager` ao CDI; os repositórios podem ter construtores `@Inject` que recebem esse recurso. Isso **não transforma** `EntityManager` em objeto seguro para uso arbitrário entre threads; o contexto de persistência é gerenciado pelo servidor.

**Spring paralelo:** configuração `@Bean` em uma classe `@Configuration` oferece um objeto ao contêiner; o mecanismo e as regras de lifecycle são diferentes.

## 7. Eventos CDI e interceptores

Eventos desacoplam componentes na mesma aplicação:
```java
@Inject Event<ClienteCriado> eventos;

public void cadastrar(NovoCliente entrada) {
    // ... persistir ...
    eventos.fire(new ClienteCriado(entrada.email()));
}
```

```java
public void aoCriar(@Observes ClienteCriado evento) {
    // notificação / auditoria local
}
```

**Cuidado:** evento CDI síncrono não equivale a Kafka, não garante persistência de mensagens, nem substitui uma transação distribuída.

Interceptors permitem aplicar comportamento transversal:
```java
@AroundInvoke
Object medir(InvocationContext ctx) throws Exception {
    long inicio = System.nanoTime();
    try { return ctx.proceed(); }
    finally { metricas.registrar(System.nanoTime() - inicio); }
}
```

A classe interceptora precisa de binding e configuração/ativação conforme CDI; este fragmento mostra somente o método. Comparativo com Spring: AOP por proxies é um conceito similar, com regras de interceptação distintas.

## 8. Problema real reproduzido no laboratório

No deploy GlassFish, `ClienteUseCase` tinha múltiplos beans elegíveis: o serviço CDI e a fachada EJB que implementava uma subinterface. O contêiner acusou **ambiguous dependencies**. A correção é explicitar o candidato desejado com qualifier na implementação de caso de uso e no ponto de injeção da fachada.

**Como diagnosticar:** leia a mensagem do Weld, identifique **tipo**, **qualifiers requeridos** e **beans candidatos**. Não instancie com `new` para “resolver”, pois isso remove o objeto do gerenciamento que deveria aplicar transações e escopos.

## 9. Perguntas com respostas comentadas

**1. Por que injeção por construtor melhora os testes?**  
**Resposta:** torna dependências explícitas e permite fornecer implementações fake/mock sem iniciar um servidor. A regra fica menos acoplada ao ciclo do contêiner.

**2. Injetar interface e classe concreta é a mesma coisa?**  
**Resposta:** não. Injetar `ClienteRepository` protege o service contra mudanças no adaptador JPA, respeitando DIP; injetar `JpaClienteRepository` acopla a camada de negócio à tecnologia.

**3. Dois beans implementam `Notificador`. O que faço?**  
**Resposta:** defino e aplico um qualifier CDI no bean e no ponto de injeção, ou faço outra configuração de resolução válida. Sem desambiguação, a aplicação pode falhar no deployment.

**4. Posso guardar o ID do usuário corrente em `@ApplicationScoped`?**  
**Resposta:** não é adequado: o bean é compartilhado no contexto de aplicação e pode expor estado de um usuário a outros. Prefira um contexto individual apropriado e evite estado mutável compartilhado.

**5. Para que serve `@Produces` com `EntityManager`?**  
**Resposta:** oferece o recurso gerenciado à resolução CDI, permitindo recebê-lo no construtor do repositório. Isso não significa que o EntityManager seja thread-safe ou precise ser criado manualmente.

**6. `@ApplicationScoped` abre uma transação?**  
**Resposta:** não. Ele controla contexto de vida. Para transação declarativa em CDI, use `jakarta.transaction.Transactional` no método elegível; no EJB, utilize CMT.

**7. Se o projeto precisa de transações, ele obrigatoriamente precisa de EJB?**  
**Resposta:** não. CDI + `jakarta.transaction.Transactional` é suficiente para muitos casos. Escolha EJB se precisar de capacidades específicas ou se for o padrão do sistema legado.

**8. Como resolver a ambiguidade `ClienteUseCase` da fachada EJB sem violar DIP?**  
**Resposta:** use um qualifier que identifique o bean de service desejado, mantendo a **interface** no campo injetado; não injete a classe concreta por conveniência.

Para exemplos de começo/commit e impacto dos atributos, veja [CDI × EJB × JTA](11-cdi-ejb-transacoes-sem-confusao.md).

## Fontes oficiais

- [Jakarta CDI](https://jakarta.ee/specifications/cdi/)
- [Jakarta Inject](https://jakarta.ee/specifications/dependency-injection/)
- [Spring — Dependency Injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)
