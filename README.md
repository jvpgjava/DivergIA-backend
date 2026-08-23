# DivergIA — Backend

Sistema que compara um texto original com uma versão editada por inteligência
artificial generativa, identifica se o sentido, a posição ou a intensidade da
mensagem foram alterados além do que uma correção de estilo justificaria, e
sugere uma reescrita alternativa fiel ao sentido original.

O roadmap de desenvolvimento completo, dividido em fases, está em
[`README_backend_divergia.md`](README_backend_divergia.md). Este documento
cobre apenas o que já existe implementado e como rodar.

---

## Stack

- Java 21 (LTS) + Spring Boot 4.1
- PostgreSQL 16+ com extensão `pgvector`
- LangChain4j (integração com LLM/RAG — portas definidas a partir da Fase 2)
- Maven, com Wrapper (`mvnw`/`mvnw.cmd`) — não é necessário ter Maven instalado
- Sem Docker em nenhuma etapa (dev, teste ou produção)

## Arquitetura

Hexagonal (Ports & Adapters):

```
com.divergia
├── domain            → entidades e regras de negócio puras, sem framework
├── application        → portas (in/out) e casos de uso
├── adapter             → web, LLM, base vetorial, extração de documento,
│                         persistência (JPA), segurança
└── config                → beans de configuração Spring
```

Regra de dependência: `adapter → application → domain`, nunca o inverso.

## O que já existe (Fase 0)

- Estrutura de pacotes hexagonal com `package-info.java` documentando cada pacote
- Virtual threads habilitadas (`spring.threads.virtual.enabled=true`)
- Profiles `dev` / `test` / `prod`, segredos de banco via variável de ambiente
- `/actuator/health` (público), incluindo checagem de conexão com o Postgres
- Documentação OpenAPI/Swagger (pública): `/v3/api-docs` e `/swagger-ui/index.html`
- Spring Security configurado para negar por padrão qualquer rota não
  explicitamente liberada (só health e docs são públicos por enquanto —
  login/JWT chegam na Fase 3)
- Suíte de testes cobrindo o contrato REST acima (`RestEndToEndTest`)
- Domínio modelado (`Usuario`, `Consentimento`, `Analise`, `TrechoDeriva`,
  `ExemploRag`) com entidades JPA equivalentes, mapper explícito entre as
  duas, e migrations Flyway (`V1`–`V6`) — incluindo a coluna vetorial
  (`pgvector`, 768 dimensões) de `exemplo_rag`
- Regra de retenção de dado (`PoliticaRetencaoDeTexto`) modelada como
  serviço de domínio puro: texto bruto de uma análise só é mantido se o
  usuário consentiu

## Pré-requisitos

- JDK 21
- PostgreSQL 16+ com a extensão `pgvector` instalada nativamente (sem
  container) — passo a passo completo em [`INSTALL.md`](INSTALL.md)

## Configuração rápida do banco (dev/test)

```sql
CREATE ROLE divergia WITH LOGIN PASSWORD 'divergia' NOSUPERUSER NOCREATEDB NOCREATEROLE;
CREATE DATABASE divergia OWNER divergia;
CREATE DATABASE divergia_test OWNER divergia;
\c divergia
CREATE EXTENSION IF NOT EXISTS vector;
\c divergia_test
CREATE EXTENSION IF NOT EXISTS vector;
```

Detalhes por sistema operacional (Linux/Windows) em [`INSTALL.md`](INSTALL.md).

## Variáveis de ambiente

| Variável      | Uso                   | Default no perfil `dev`                     |
|---------------|------------------------|-----------------------------------------------|
| `DB_URL`      | JDBC URL do Postgres   | `jdbc:postgresql://localhost:5432/divergia`    |
| `DB_USERNAME` | Usuário do banco       | `divergia`                                     |
| `DB_PASSWORD` | Senha do banco         | `divergia`                                     |

No perfil `prod` as três são obrigatórias (sem default) — nunca commitar
valor real; usar variável de ambiente do processo (systemd `Environment=`)
ou arquivo local coberto pelo `.gitignore`.

## Rodando a aplicação

```bash
# perfil dev (padrão), contra o Postgres local
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`:

- Health check: `GET http://localhost:8080/actuator/health`
- Documentação interativa (Swagger UI): `http://localhost:8080/swagger-ui/index.html`
- Especificação OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

Qualquer outra rota exige autenticação (ainda não implementada — chega na
Fase 3) e responde `401`/`403`.

## Rodando os testes

Os testes de integração rodam contra o Postgres real (perfil `test`, banco
`divergia_test`) — não usam Testcontainers nem H2 (H2 não suporta pgvector).

```bash
./mvnw test
```

## Build

```bash
./mvnw package
```

Gera o JAR executável autocontido em `target/`.
