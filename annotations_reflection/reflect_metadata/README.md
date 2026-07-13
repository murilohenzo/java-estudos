# reflect_metadata — Comparativo Java ↔ TypeScript (`reflect-metadata`)

> Lado a lado: a lib [microsoft/reflect-metadata](https://github.com/microsoft/reflect-metadata)  
> vs annotations/reflection do JDK vs uma implementação **educacional** em TypeScript **5.3.3** puro  
> e um espelho de API em Java.

---

## Estrutura (espelho da ideia da lib)

```
reflect_metadata/
├── README.md                          ← este comparativo
├── typescript/                        ← mini lib estilo reflect-metadata (TS 5.3.3)
│   ├── package.json                   # typescript@5.3.3
│   ├── tsconfig.json
│   └── src/
│       ├── reflect-metadata.ts        # WeakMap + API Reflect.*
│       └── demo.ts                    # DI manual + decorators
└── java/                              ← mesma superfície de API em Java
    └── src/br/com/murilo/reflectmeta/
        ├── MetadataReflect.java
        └── MetadataReflectDemo.java
```

Modelo de dados (igual à proposta / lib oficial):

```
target → (propertyKey | undefined) → (metadataKey → metadataValue)
```

- **TS:** `WeakMap<object, Map<PropertyKey|undefined, Map<MetadataKey, unknown>>>`
- **Java:** `ConcurrentHashMap<MemberKey(class, property?), ConcurrentHashMap<key, value>>`

---

## Comparativo

| Conceito | `reflect-metadata` (oficial) | Nossa TS 5.3.3 | Java nativo | Nosso `MetadataReflect` |
|---|---|---|---|---|
| Metadado declarativo | Decorators + `@Reflect.metadata` | `Reflect.metadata(...)` | `@interface` + `@Retention(RUNTIME)` | Só imperativo (`defineMetadata`) |
| API imperativa | `define/get/has/deleteMetadata` | ✅ subset | `AnnotatedElement` | ✅ mesmos nomes |
| Store | WeakMap interno | WeakMap | Class file / VM | ConcurrentHashMap |
| Cadeia de herança | Prototype chain | `Object.getPrototypeOf` | `getSuperclass()` | `getSuperclass()` loop |
| Tipos de ctor em runtime | `emitDecoratorMetadata` → `design:paramtypes` | Manual via `defineMetadata` | `Constructor.getParameterTypes()` | Simulado com `design:paramtypes` |
| Usado por | NestJS, TypeORM, Angular (legado) | Demo educacional | Spring, Hibernate, JUnit | Demo educacional |
| Precisa de polyfill? | Sim (JS sem annotations) | N/A (é o polyfill didático) | Não | Não (didático) |

### Por que a lib existe no TypeScript e não no Java?

```
TypeScript/JavaScript          Java
─────────────────────────      ─────────────────────────────
Sem annotations nativas   →    @Component / @Inject no bytecode
Tipos apagados no emit    →    tipos de parâmetro no .class
(emitDecoratorMetadata)        (getParameterTypes)

Decorator + reflect-metadata   AnnotatedElement + Reflection API
grava Map em runtime           lê atributos já gravados pelo javac
```

O **container Nest** faz:

```ts
Reflect.getMetadata("design:paramtypes", UserService);
```

O **Spring / mini_ioc** faz:

```java
ctor.getParameterTypes(); // + @Inject
```

Mesmo problema (descobrir dependências); mecanismos diferentes.

---

## API espelhada

| Oficial / nossa TS | Java `MetadataReflect` |
|---|---|
| `Reflect.defineMetadata(k, v, target)` | `defineMetadata(k, v, Class)` |
| `Reflect.defineMetadata(k, v, target, prop)` | `defineMetadata(k, v, Class, prop)` |
| `Reflect.getMetadata` / `getOwnMetadata` | iguais |
| `Reflect.hasMetadata` / `hasOwnMetadata` | iguais |
| `Reflect.getMetadataKeys` / `getOwnMetadataKeys` | iguais |
| `Reflect.deleteMetadata` | `deleteMetadata` |
| `@Reflect.metadata(k, v)` | — (use annotations reais ou `defineMetadata`) |

---

## Como rodar

### TypeScript 5.3.3

```bash
cd annotations_reflection/reflect_metadata/typescript
npm install
npm run demo
```

### Java (JDK 21+)

```bash
cd annotations_reflection/reflect_metadata/java/src
javac br/com/murilo/reflectmeta/*.java
java br.com.murilo.reflectmeta.MetadataReflectDemo
```

---

## Relação com o restante do módulo

| Pasta | Papel |
|---|---|
| [`../reflection/`](../reflection/README.md) | Ler annotations nativas do Java |
| [`../mini_ioc/`](../mini_ioc/README.md) | Container que **usa** reflection (como Nest usa reflect-metadata) |
| **`reflect_metadata/`** | Entender a **camada de metadados** que o Nest empilha em cima do JS |

Fluxo mental:

```
TS:  @Injectable → reflect-metadata store → Nest container
Java: @Component → AnnotatedElement / MetadataReflect → mini_ioc
```

---

## Limitações (proposital)

- Sem polyfill completo da lib oficial (Proxy traps, etc.)
- TS demo **não** liga `emitDecoratorMetadata` — metadados são explícitos para o aprendizado
- Java store não é WeakReference (educacional; classes vivem o tempo do ClassLoader)
- Não substitui `microsoft/reflect-metadata` em projetos reais
