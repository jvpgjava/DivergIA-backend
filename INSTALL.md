# Instalação do PostgreSQL + pgvector (sem container)

Este projeto não usa Docker em nenhuma etapa. O PostgreSQL é instalado
nativamente, tanto na máquina de desenvolvimento quanto na VPS de produção.

## Linux (Ubuntu/Debian — recomendado, espelha a VPS de produção)

```bash
# 1. Repositório oficial do PostgreSQL (apt.postgresql.org)
sudo apt install -y curl ca-certificates
sudo install -d /usr/share/postgresql-common/pgdg
sudo curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc \
  https://www.postgresql.org/media/keys/ACCC4CF8.asc
sudo sh -c 'echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] \
  https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" \
  > /etc/apt/sources.list.d/pgdg.list'
sudo apt update

# 2. PostgreSQL 16 + extensão pgvector
sudo apt install -y postgresql-16 postgresql-16-pgvector

# 3. Serviço
sudo systemctl enable --now postgresql

# 4. Criar usuário e bancos (dev e test), com privilégio restrito ao schema
sudo -u postgres psql <<'SQL'
CREATE ROLE divergia WITH LOGIN PASSWORD 'divergia' NOSUPERUSER NOCREATEDB NOCREATEROLE;
CREATE DATABASE divergia OWNER divergia;
CREATE DATABASE divergia_test OWNER divergia;
\c divergia
CREATE EXTENSION IF NOT EXISTS vector;
\c divergia_test
CREATE EXTENSION IF NOT EXISTS vector;
SQL
```

> Em produção, o usuário `divergia` do banco não deve ter superusuário — a
> aplicação nunca conecta como superusuário (princípio do menor privilégio).

## Windows (máquina de desenvolvimento local)

1. Baixe o instalador oficial em https://www.postgresql.org/download/windows/
   (EDB installer, PostgreSQL 16.x) e instale normalmente — sem WSL/Docker.
2. Instale a extensão `pgvector` a partir dos binários pré-compilados do
   projeto (https://github.com/pgvector/pgvector#windows) ou compile com o
   `nmake` usando o "x64 Native Tools Command Prompt for VS", seguindo o
   `README` do próprio projeto pgvector.
3. Usando `psql` (ou pgAdmin), crie usuário e bancos:

   ```sql
   CREATE ROLE divergia WITH LOGIN PASSWORD 'divergia' NOSUPERUSER NOCREATEDB NOCREATEROLE;
   CREATE DATABASE divergia OWNER divergia;
   CREATE DATABASE divergia_test OWNER divergia;
   \c divergia
   CREATE EXTENSION IF NOT EXISTS vector;
   \c divergia_test
   CREATE EXTENSION IF NOT EXISTS vector;
   ```

## Variáveis de ambiente esperadas pela aplicação

| Variável      | Uso                                   | Default (perfil `dev`)                        |
|---------------|----------------------------------------|------------------------------------------------|
| `DB_URL`      | JDBC URL do Postgres                   | `jdbc:postgresql://localhost:5432/divergia`     |
| `DB_USERNAME` | Usuário do banco                       | `divergia`                                      |
| `DB_PASSWORD` | Senha do banco                         | `divergia`                                      |

No perfil `prod` não há default: as três variáveis são obrigatórias. Nunca
commitar valores reais — usar variável de ambiente do systemd unit
(`Environment=`/`EnvironmentFile=`) ou arquivo local coberto pelo
`.gitignore` (`.env`, `application-local.yml`).

## Configurando o e-mail de recuperação de senha (Gmail SMTP)

O Gmail não aceita a senha normal da conta para SMTP de terceiros — é
preciso gerar uma **App Password** (exige verificação em duas etapas
habilitada na conta):

1. Ative a verificação em duas etapas em https://myaccount.google.com/security
2. Gere uma senha de app em https://myaccount.google.com/apppasswords
3. Exporte as variáveis de ambiente antes de rodar a aplicação:

```bash
export GMAIL_USERNAME="seu-email@gmail.com"
export GMAIL_APP_PASSWORD="a-senha-de-16-caracteres-gerada-acima"
```

Sem essas duas variáveis, o cadastro/login continuam funcionando
normalmente — só o envio do e-mail de recuperação de senha falha.

## Gerando um JWT_SECRET forte para produção

```bash
openssl rand -base64 48
```

Qualquer saída com pelo menos 32 caracteres serve (HS256 exige isso). O
perfil `dev` já tem um valor placeholder inseguro embutido só para
desenvolvimento local — nunca usar esse mesmo valor em produção.

## Rodando a aplicação

```bash
# perfil dev (default), contra o Postgres local criado acima
./mvnw spring-boot:run

# checagem de saúde
curl http://localhost:8080/actuator/health
```

## Rodando os testes

Os testes de integração de `adapter/out/persistence` rodam contra o Postgres
local real (perfil `test`, banco `divergia_test`) — não Testcontainers, não
H2 (H2 não suporta pgvector).

```bash
./mvnw test
```
