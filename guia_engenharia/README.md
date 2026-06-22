# Guia de Boas Práticas — Java & Engenharia de Software

> Referência técnica transversal ao projeto. Estes guias complementam os módulos
> de código com fundamentos de engenharia que transcendem linguagens e frameworks.

---

## Documentos neste guia

| Documento | O que cobre |
|---|---|
| [COMPLEXIDADE_ALGORITMOS.md](./COMPLEXIDADE_ALGORITMOS.md) | Big O notation, O(1) a O(n²), operações das Collections Java, aplicação no projeto |
| [CARGA_COGNITIVA.md](./CARGA_COGNITIVA.md) | Carga cognitiva extrínseca, nomes reveladores, guard clauses, imutabilidade, Java 21 |
| [COESAO_ACOPLAMENTO.md](./COESAO_ACOPLAMENTO.md) | Alta coesão, baixo acoplamento, SOLID, DI, composição vs herança, package by feature |

---

## Relação entre os três eixos

```
                    ┌─────────────────────────────┐
                    │   SOFTWARE BEM PROJETADO     │
                    └─────────────────────────────┘
                              ▲
              ┌───────────────┼───────────────┐
              │               │               │
    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
    │ PERFORMANCE │  │MANUTENIBILI-│  │ENTENDIMENTO  │
    │             │  │    DADE     │  │             │
    │ Big O       │  │ Coesão +    │  │ Carga       │
    │ Complexidade│  │ Acoplamento │  │ Cognitiva   │
    └─────────────┘  └─────────────┘  └─────────────┘
```

### Performance sem legibilidade = bug difícil de encontrar
### Legibilidade sem performance = sistema que não escala
### Boa arquitetura com código complexo = manutenção cara

Os três eixos precisam ser balanceados. Em geral:

1. **Faça funcionar** (corretude)
2. **Faça legível** (carga cognitiva)
3. **Faça rápido** (complexidade) — apenas onde o profiling indicar gargalo

---

## Quick Reference

### Big O das Collections mais usadas

```
HashMap.get/put      → O(1)    ← padrão para busca por chave
ArrayList.get(i)     → O(1)    ← padrão para acesso aleatório
TreeMap.get/put      → O(log n)← quando precisar de ordem
List.contains        → O(n)    ← evitar em loops
Collections.sort     → O(n log n)
Loops aninhados s/índice → O(n²) ← evitar — pre-indexar com Map
```

### Sinais de baixa coesão

```
❌ Classe com mais de 200 linhas
❌ Nome com "Manager", "Helper", "Utils", "Service" que faz tudo
❌ Método que faz validação + persistência + notificação
❌ God class com 20+ métodos e dependências
```

### Sinais de alto acoplamento

```
❌ new ConcreteImpl() dentro de outra classe
❌ static mutable fields compartilhados
❌ Herança profunda (> 3 níveis)
❌ Método recebe boolean flag que controla comportamento interno
❌ Classe conhece 10+ outras classes
```

### Sinais de alta carga cognitiva

```
❌ Método com > 3 parâmetros sem objeto aglutinador
❌ Aninhamento com > 3 níveis de if/for
❌ Nomes de 1-2 letras fora de índices de loop
❌ Números mágicos (42, 30, 0.1) sem nome
❌ Comentário explicando O QUÊ o código faz (deveria ser óbvio)
```

---

## Como usar no código review

Ao revisar um PR, passe pelas três lentes:

```
1. PERFORMANCE: Existe busca linear dentro de loop? Pode indexar?
                O tipo de coleção é o adequado para o acesso esperado?

2. DESIGN:      A classe tem mais de uma razão para mudar?
                As dependências apontam para interfaces?
                Composição ou herança?

3. LEGIBILIDADE: Nomes revelam intenção?
                 Profundidade de aninhamento?
                 Parâmetros agrupados em objeto?
                 Java 21 features reduzem boilerplate?
```
