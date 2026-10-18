# 02 — MVC na prática: Spring MVC versus JSF/Jakarta EE

[← Índice](README.md) · [← Fundamentos](01-fundamentos-spring-jakarta.md) · [JSF →](03-jsf-primefaces.md)

## 1. Qual é a ideia de MVC?

**Model** é a representação do estado e das regras; **View** apresenta a interface; **Controller** recebe uma intenção e coordena uma ação sobre o model.

Não confunda MVC com **arquitetura em camadas**: Controller não deve conter SQL, View não deve conter regras de negócio e a entidade JPA não deveria decidir como responder HTTP.

### MVC que usamos no laboratório

| Papel | Tecnologia | Classe/arquivo |
|---|---|---|
| View | JSF + PrimeFaces | `cliente.xhtml` |
| Controller de cliente | CDI/JSF `@Named @ViewScoped` | `ClienteBean` |
| Controller de pedido | CDI/JSF `@Named @ViewScoped` | `PedidoBean` |
| Model: casos de uso | Interfaces + serviço | `ClienteBoundary`, `ClienteApplicationService` |
| Fronteira transacional | EJB | `ClienteFacadeEJB` |
| Persistência | JPA | `JpaClienteRepository`, `EntityManager` |

A separação de ports/adapters complementa MVC: o serviço conhece a interface do repositório, não a classe JPA concreta.

## 2. Spring MVC tradicional: fluxo HTTP → Controller → Service → Repository → View

```mermaid
sequenceDiagram
    actor U as Navegador
    participant DS as DispatcherServlet
    participant C as @Controller
    participant S as @Service
    participant R as Repository
    participant DB as Banco
    U->>DS: GET /clientes
    DS->>C: HandlerMapping / invoca Controller
    C->>S: listar()
    S->>R: buscar()
    R->>DB: SELECT
    DB-->>R: Registros
    R-->>S: Lista
    S-->>C: DTOs
    C-->>DS: Model e nome da View
    DS-->>U: ViewResolver + HTML
```

Exemplo Spring MVC com template:
```java
@Controller
@RequestMapping("/clientes")
class ClienteController {
    private final ClienteConsulta consulta;

    ClienteController(ClienteConsulta consulta) {
        this.consulta = consulta;
    }

    @GetMapping
    String listar(Model model) {
        model.addAttribute("clientes", consulta.listar());
        return "clientes"; // resolvido por mecanismo de View
    }
}
```

**Se for `@RestController`**, o retorno geralmente vira corpo HTTP (por conversores de mensagem), **sem** resolver uma View HTML.

## 3. JSF: fluxo HTTP → FacesServlet → árvore de componentes → Bean → Service → renderização

```mermaid
sequenceDiagram
    actor U as Navegador
    participant FS as FacesServlet / lifecycle
    participant X as Componentes XHTML
    participant C as @Named ClienteBean
    participant E as EJB transacional
    participant S as Service CDI
    participant J as JPA Repository
    U->>FS: POST /cliente.xhtml
    FS->>X: Restaurar view e aplicar dados
    FS->>X: Converter e validar
    X->>C: Atualizar propriedades de bean
    FS->>C: action salvar()
    C->>E: cadastrar(DTO)
    E->>S: executar caso de uso
    S->>J: salvar(cliente)
    J-->>S: entidade gerenciada
    E-->>C: resultado
    C-->>FS: outcome (pode ser null)
    FS-->>U: Renderizar componentes/HTML
```

**O que muda:** no JSF, a **View é componente server-side**, e o framework coordena fases de conversão, validação, model update e renderização. `FacesServlet` é o *front controller* que orquestra o lifecycle; `ClienteBean` é o controlador específico da tela/ação.

Em Spring MVC, o `DispatcherServlet` orquestra mappings, resolvers e adaptadores; um método `@Controller` costuma receber parâmetros de requisição já convertidos e validados via binding e Bean Validation, sem árvore de componentes JSF.

## 3.1 Duas maneiras de chamar o service no Jakarta EE

O diagrama acima mostra o **fluxo escolhido no laboratório**: o EJB é fronteira CMT e o service CDI é executado dentro dessa transação. **Esse EJB não é obrigatório**. Alternativamente, o JSF Controller pode chamar diretamente um `ClienteUseCase` implementado por CDI, cujo método tenha `jakarta.transaction.Transactional(REQUIRED)`.

```mermaid
flowchart LR
  V["Controller JSF"] --> D{"Estratégia de transação"}
  D --> A["CDI @Transactional: limite no service"]
  D --> B["EJB @Stateless CMT: limite na fachada"]
  B --> C["CDI service sem nova demarcação"]
  A --> R["Repository"]
  C --> R
  R --> DB[("Banco")]
```

**Não misture sem motivo** `@Transactional(REQUIRES_NEW)` no service com `REQUIRED` na fachada: isso mudaria a unidade de commit, pois a operação interna ganharia outra transação. Estude a [tabela de decisão CDI/EJB/JTA](11-cdi-ejb-transacoes-sem-confusao.md).

## 4. Dois exemplos da mesma operação

**Jakarta Faces:**
```xml
<h:form id="cadastro">
    <h:inputText id="nome" value="#{clienteBean.nome}" required="true"/>
    <h:commandButton value="Salvar" action="#{clienteBean.salvar}"/>
</h:form>
```

```java
@Named
@ViewScoped
public class ClienteBean implements Serializable {
    private String nome;
    @Inject
    private ClienteBoundary cliente; // interface exposta pela fachada EJB

    public String salvar() {
        cliente.cadastrar(new NovoCliente(nome, "exemplo@site.com"));
        return null; // mesma view
    }
    // getters/setters omitidos neste trecho didático
}
```

**Spring MVC com Thymeleaf (forma equivalente em intenção, não em lifecycle):**
```java
@Controller
@RequestMapping("/clientes")
class ClienteController {
    private final ClienteUseCase casoDeUso;

    ClienteController(ClienteUseCase casoDeUso) { this.casoDeUso = casoDeUso; }

    @PostMapping
    String salvar(@Valid @ModelAttribute ClienteForm form, BindingResult erros) {
        if (erros.hasErrors()) return "clientes-form";
        casoDeUso.cadastrar(form.toCommand());
        return "redirect:/clientes";
    }
}
```

Os trechos são **pseudocódigo pedagógico abreviado**; imports, métodos de modelo e templates precisam ser completados para execução.

## 5. REST e SPA não são a mesma coisa que JSF

- **JSF:** mantém contexto de view/componentes do lado servidor; `action`, `ViewScoped`, AJAX parcial.
- **Spring REST:** fornece endpoints HTTP que devolvem JSON; o React/Angular administra a interface.
- **Jakarta REST:** também fornece endpoints HTTP JSON sem obrigar uso de JSF.
- **Composição brownfield:** uma aplicação Java EE pode manter telas JSF e expor uma nova API para outra interface.

```mermaid
flowchart TB
    B["Browser"] --> JSF["JSF / XHTML"]
    B --> SPA["React / Angular"]
    JSF --> E["Serviços existentes"]
    SPA --> REST["API Jakarta REST ou Spring MVC"]
    REST --> E
    E --> DB[("Persistência")]
```

## 6. Perguntas de diagnóstico com gabarito comentado

**1. O botão JSF foi clicado, mas `salvar()` não foi executado. Onde investigar primeiro?**  
**Resposta:** no **lifecycle JSF**: formulário `h:form`, conversões, validações, `process`, resolução EL e escopo. Uma falha de validação pode impedir a fase Invoke Application mesmo quando houve POST HTTP.

**2. Em Spring MVC, qual componente recebe a requisição antes do `@Controller`?**  
**Resposta:** o `DispatcherServlet`, responsável por encaminhar a requisição para o handler adequado e coordenar a infraestrutura MVC.

**3. No JSF, `ClienteBean` substitui o `FacesServlet`?**  
**Resposta:** não. O `FacesServlet` é o front controller da tecnologia; o bean contém ações e propriedades específicas daquela tela.

**4. Onde deve estar a transação de cadastrar cliente e pedido?**  
**Resposta:** na fronteira do caso de uso: **CDI `@Transactional` ou EJB CMT REQUIRED**, não obrigatoriamente no controller JSF. Os repositórios participam da transação iniciada no serviço/fachada.

**5. Houve SQL, mas nenhum dado permaneceu no banco. Isso prova falha de JPA?**  
**Resposta:** não. Pode ter ocorrido `flush` seguido de **rollback**; é necessário conferir limites transacionais, logs e exceções.

**6. A action funcionou, mas a tabela visual não atualizou. Onde olhar?**  
**Resposta:** no resultado de renderização JSF, em `update` AJAX, IDs de componentes, no escopo do bean e na view restaurada, antes de atribuir o problema à persistência.

## Fontes

- [Spring MVC — DispatcherServlet](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet.html)
- [Spring MVC — Controllers](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html)
- [Jakarta Faces — Lifecycle](https://jakarta.ee/learn/docs/jakartaee-tutorial/current/web/faces-intro/faces-intro.html)
- [Jakarta EE — Web Applications](https://jakarta.ee/learn/docs/jakartaee-tutorial/current/web/webapp/webapp.html)
