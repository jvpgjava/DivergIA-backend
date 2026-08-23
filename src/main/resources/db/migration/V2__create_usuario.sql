CREATE TABLE usuario (
    id          UUID PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    senha_hash  VARCHAR(255) NOT NULL,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);
