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

## O que já existe (Fases 0–8)

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
- `LlmPort` e `VectorStorePort` (`application/port/out`), sem que nenhum
  caso de uso importe LangChain4j
- Adapter de LLM (`adapter/out/llm`) via módulo OpenAI-compatible do
  LangChain4j, apontado para a Abacus.AI/RouteLLM (modelo `claude-sonnet-5`)
- Adapter de vetor (`adapter/out/vectorstore`) via LangChain4j + Gemini
  (`gemini-embedding-001`, 768 dimensões — a Abacus.AI/RouteLLM não oferece
  embeddings) sobre a tabela `exemplo_rag`/pgvector já criada na Fase 1
- Testes de integração de ambos os adapters contra um servidor HTTP local
  que imita as APIs reais — nenhuma chamada de rede real acontece em
  `./mvnw test`/CI
- Autenticação completa (RF01–RF05): cadastro (BCrypt custo 12), login
  (JWT HS256, expiração curta), logout real (blacklist de `jti` em banco —
  necessário porque JWT é stateless), recuperação de senha (token de uso
  único com hash SHA-256, e-mail via SMTP do Gmail) e exclusão de conta
  (cascata sobre análises/consentimentos)
- Rate limiting em memória (Bucket4j) em `/api/auth/login` e
  `/api/auth/cadastro` — 5 tentativas/minuto por IP
- Endpoints REST em `/api/auth/*`, já documentados automaticamente no
  Swagger (nenhum passo manual)
- Microsserviço Python isolado (`extraction-service/`, FastAPI + Docling)
  extraindo texto de documentos (PDF, DOCX, etc.) via `POST /extrair`, com
  `GET /health` próprio — sem container, `venv` nativo
- `ExtracaoDocumentoPort` (`application/port/out`) + adapter
  (`adapter/out/extraction`) chamando o serviço Python via `RestClient`
  com timeout configurável; nenhuma outra classe conhece o contrato HTTP
  desse serviço
- **Análise comparativa (RF06–RF15)**: `POST /api/analises` (autenticado),
  aceitando texto colado OU upload de arquivo independentemente para cada
  lado (original/editado) — validação → extração (se arquivo) → busca de
  exemplos via RAG → avaliação pelo LLM → persistência. Regra de retenção
  aplicada de fato: sem consentimento, nem o texto da análise nem os
  trechos de deriva (que também citam texto bruto) são persistidos — mas
  o resultado é sempre devolvido na resposta
- Rate limiting também em `/api/analises` (10/min por IP — cada chamada
  tem custo real de LLM), reaproveitando o mesmo filtro da Fase 3
- **Sugestão de reescrita fiel (RF16–RF17)**: `POST /api/analises/trechos/{trechoId}/sugestao-reescrita`
  (autenticado) — reaproveita `LlmPort`/`VectorStorePort`, sem porta nova.
  Verifica que o trecho pertence a uma análise do próprio usuário (403 se
  não, 404 se o trecho não existir/não foi persistido)
- **Histórico, tendência e privacidade (RF18–RF29)**: CRUD de histórico
  (`GET/DELETE /api/historico`, `GET/DELETE /api/historico/{id}`), painel
  de tendência pessoal (`GET /api/historico/tendencia` — totais,
  distribuição por tipo de desvio, evolução mês a mês) e consentimento
  (`GET/PUT /api/consentimento`)
- **Consentimento com dois campos distintos**: `manterHistorico` (guardar
  o próprio histórico) e `contribuirParaRag` (deixar trechos serem usados
  como referência nas análises de QUALQUER usuário) — são decisões de
  privacidade diferentes, cada uma com seu próprio opt-in
- Job assíncrono (`@Scheduled`, cron configurável) que promove trechos de
  deriva para a base RAG compartilhada — só quando o usuário consentiu
  especificamente em `contribuirParaRag`; testado de ponta a ponta com
  LLM/embedding reais, incluindo o caso negativo (usuário sem esse
  consentimento não tem nada promovido, mas o trecho é marcado como
  processado para não ser reavaliado todo dia)
- **Observabilidade**: `@RestControllerAdvice` único (`GlobalExceptionHandler`)
  padronizando todo erro da API num shape só (`timestamp/status/error/message/path`)
  — inclusive erros de validação (`@Valid`) e um handler genérico que nunca
  vaza stack trace/detalhe interno ao cliente
- Logs estruturados com rotação (`logback-spring.xml`): console em
  `dev`/`test`, console + arquivo com rotação diária/por tamanho (10MB,
  30 dias, cap de 1GB) em `prod`. Auditoria confirmou: nenhum log
  referencia texto bruto de análise (corrigido um vazamento em
  `AbacusLlmAdapter` que embutia a resposta crua do LLM na mensagem de erro)
- Swagger com esquema de autenticação Bearer JWT configurado (botão
  "Authorize" funcional) e tags por controller

## Variáveis de ambiente adicionais (Fase 2)

| Variável           | Uso                                      |
|--------------------|-------------------------------------------|
| `ABACUS_API_KEY`   | Chave da Abacus.AI/RouteLLM (LLM)          |
| `ABACUS_BASE_URL`  | Default `https://routellm.abacus.ai/v1`    |
| `ABACUS_MODEL_NAME`| Default `claude-sonnet-5`                  |
| `GOOGLE_API_KEY`   | Chave do Google AI Studio (embedding Gemini) |
| `GEMINI_MODEL_NAME`| Default `gemini-embedding-001`             |

## Variáveis de ambiente adicionais (Fase 3)

| Variável              | Uso                                            | Default no perfil `dev`      |
|------------------------|--------------------------------------------------|--------------------------------|
| `JWT_SECRET`           | Chave de assinatura do JWT (HS256, mín. 32 chars) | placeholder inseguro (só dev)  |
| `JWT_EXPIRACAO_MINUTOS`| Validade do access token                          | `15`                            |
| `GMAIL_USERNAME`       | Conta Gmail usada para enviar e-mail de recuperação | vazio (envio falha sem isso)  |
| `GMAIL_APP_PASSWORD`   | *App Password* do Gmail (não é a senha da conta)  | vazio                           |

No perfil `prod`, `JWT_SECRET` é obrigatório (sem default) — igual às
credenciais de banco.

## Variáveis de ambiente adicionais (Fase 4)

| Variável                    | Uso                                   | Default              |
|------------------------------|------------------------------------------|-------------------------|
| `EXTRACAO_BASE_URL`          | URL do microsserviço de extração          | `http://localhost:8000` |
| `EXTRACAO_TIMEOUT_SEGUNDOS`  | Timeout de conexão/leitura da chamada HTTP | `30`                     |

## Variáveis de ambiente adicionais (Fase 7)

| Variável             | Uso                                              | Default                |
|------------------------|-----------------------------------------------------|---------------------------|
| `PROMOCAO_RAG_CRON`   | Cron do job de promoção de exemplos ao RAG           | `0 0 3 * * *` (3h da manhã) |

## Microsserviço de extração de documento

Fica em [`extraction-service/`](extraction-service/) — Python (FastAPI +
Docling), instruções completas de setup/execução no
[README próprio](extraction-service/README.md). Resumo:

```bash
cd extraction-service
python -m venv venv
venv\Scripts\Activate.ps1   # Windows; Linux/macOS: source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --port 8000
```

O backend Java espera esse serviço em `http://localhost:8000` por padrão
(`EXTRACAO_BASE_URL`).

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

Endpoints de autenticação: `POST /api/auth/{cadastro,login,recuperar-senha,redefinir-senha}`
(públicos) e `POST /api/auth/logout` / `DELETE /api/auth/conta` (exigem
`Authorization: Bearer <token>`). Qualquer outra rota exige autenticação e
responde `401`/`403`.

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
