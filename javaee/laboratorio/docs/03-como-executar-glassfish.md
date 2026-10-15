# 03 — Subindo GlassFish e PostgreSQL: do zero

> **Objetivo:** fazer o laboratório rodar usando o servidor de aplicação de verdade. Todos os passos são para desenvolvimento local.

## Glossário dummy

- **Contêiner Docker:** um processo isolado com tudo de que um serviço precisa. É como uma caixa de ferramentas pronta.
- **Imagem:** a receita pronta para criar o contêiner. `Dockerfile` descreve como montar uma imagem.
- **Docker Compose:** configura e conecta várias caixas — aqui, **GlassFish** e **PostgreSQL**.
- **GlassFish:** o servidor que cria e gerencia os objetos Java corporativos, as transações, as conexões e as páginas web.
- **DataSource:** uma torneira de conexões de banco gerenciada pelo servidor.
- **Connection Pool:** conjunto de conexões reutilizáveis; evita abrir uma conexão física a cada requisição.
- **JNDI (`jdbc/EstudosDS`):** nome pelo qual a aplicação procura o DataSource no servidor.
- **WAR:** arquivo que empacota código Java, bibliotecas, configurações e páginas XHTML para publicação.
- **Deploy:** ato de entregar o WAR ao GlassFish para que a aplicação fique disponível.

## Pré-requisitos

Instalar **Docker Engine com Compose v2** (ou Docker Desktop). Opcionalmente Maven e JDK 17 para rodar testes fora dos contêineres.

## Executar tudo

A partir da raiz do repositório:

```bash
cd javaee/laboratorio
cp .env.example .env
docker compose up --build
```

O Compose inicia o banco, espera o healthcheck passar e inicia o GlassFish. O build Maven e o driver PostgreSQL fazem parte do Dockerfile. O script `docker/glassfish/init.sh` cria o pool **EstudosPool**, registra `jdbc/EstudosDS` e testa a conexão antes do deploy.

Acessos:

- Aplicação: http://localhost:8080/javaee-laboratorio/cliente.xhtml
- Administração GlassFish: https://localhost:4848 (pode apresentar aviso de certificado de desenvolvimento).

As portas ficam vinculadas somente ao `127.0.0.1` do computador hospedeiro. Credenciais padrão são **exclusivamente didáticas** e devem ser alteradas em ambientes compartilhados.

## Verificar o ambiente

```bash
docker compose ps
docker compose logs -f glassfish
docker compose logs -f banco
```

Se a aplicação não abrir, procurar primeiro por erro de Maven, de `create-jdbc-connection-pool`, `ping-connection-pool` e por falhas de deploy ou de JPA.

## Reiniciar, parar e limpar

```bash
docker compose down
docker compose up --build
# DESCARTA TAMBÉM O BANCO; usar somente quando quiser resetar os dados:
docker compose down -v
```

**Atenção:** o `persistence.xml` do laboratório utiliza a ação de schema `create`: tabelas de estudo podem ser recriadas a cada novo deployment. Mesmo que o volume PostgreSQL sobreviva ao `down`, isso **não significa que os registros serão preservados**. Não usar esta configuração em produção.

## O que os arquivos de infraestrutura fazem

| Arquivo | Responsabilidade |
|---|---|
| `Dockerfile` | Compilar WAR, baixar JDBC e criar imagem de aplicação |
| `compose.yaml` | Orquestrar GlassFish e PostgreSQL |
| `docker/glassfish/init.sh` | Provisionar recurso JDBC e testar o pool |
| `src/main/resources/META-INF/persistence.xml` | Dizer ao JPA onde encontrar a conexão JTA |
| `src/main/webapp/WEB-INF/web.xml` | Registrar o FacesServlet |
| `src/main/webapp/WEB-INF/beans.xml` | Habilitar descoberta de beans CDI |

## Problemas comuns

**Erro ao criar o pool:** verificar se o driver PostgreSQL foi copiado ao classpath do domínio e se o banco está pronto. **Erro JNDI `jdbc/EstudosDS`:** verificar se o script de inicialização foi executado. **Erro de namespace:** o projeto usa `jakarta.*` e GlassFish 7.1 (Jakarta EE 10); não misturar APIs legadas `javax.faces` com as modernas. **Deploy falhou:** inspecionar logs do contêiner, stacktrace e compatibilidade de versões.

## Fontes

- [GlassFish Docker — uso, deploy e inicialização](https://github.com/eclipse-ee4j/glassfish.docker/wiki/Example:-Using-GlassFish-Server-with-the-docker-Command)
- [Compatibilidade de versões do GlassFish](https://github.com/eclipse-ee4j/glassfish)
- [Docker Compose](https://docs.docker.com/compose/)
