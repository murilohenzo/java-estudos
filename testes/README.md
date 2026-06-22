# testes — Testes Automatizados em Java

> Cobre o espectro completo de testes: do `@Test` mais básico até arquiteturas de
> test doubles e test suites com injeção de dependência manual.

---

## Submódulos

| Diretório | Conteúdo | Nível |
|---|---|---|
| [`junit4/`](./junit4/README.md) | JUnit 4 anatomy, `@Test`, `Assert.*`, lifecycle (`@Before`, `@AfterClass`), timeout/expected exception | Fundamentos |
| [`service_dao/`](./service_dao/README.md) | Service + DAO pattern, Test Doubles (Dummy/Stub/Mock/Spy/Fake), constructor injection, `@RunWith(Suite)` | Intermediário |

---

## Progressão de aprendizado

```
junit4/                          service_dao/
  @Test básico               →     Test Doubles para isolar dependências
  Assert.assertEquals        →     Constructor injection para testabilidade
  @Before / @After           →     Mock vs Stub vs Fake — quando usar cada um
  @Test(expected=...)        →     Suite organizando múltiplos testes
  @Test(timeout=...)         →     Teste de Service sem banco de dados real
```

---

## Test Doubles — resumo

| Tipo | Comportamento | Quando usar |
|---|---|---|
| **Dummy** | Passado mas nunca usado | Preencher lista de parâmetros obrigatórios |
| **Stub** | Retorna valor fixo | Simular resposta de dependência externa |
| **Mock** | Verifica se foi chamado corretamente | Validar interação, não resultado |
| **Spy** | Parcialmente real, parcialmente controlado | Observar chamadas em objeto real |
| **Fake** | Implementação simplificada funcional | Banco em memória, filesystem fake |

---

## JUnit 4 vs JUnit 5

| Aspecto | JUnit 4 | JUnit 5 |
|---|---|---|
| Annotation import | `org.junit.*` | `org.junit.jupiter.api.*` |
| Exceção esperada | `@Test(expected=X.class)` | `assertThrows(X.class, () → ...)` |
| Lifecycle | `@Before`, `@After` | `@BeforeEach`, `@AfterEach` |
| Suite | `@RunWith(Suite.class)` | `@Suite` |
| Nested tests | ❌ | `@Nested` |

Para JUnit 5 em ação, ver `orm/` (testes de integração JPA/Hibernate/JDBI).
