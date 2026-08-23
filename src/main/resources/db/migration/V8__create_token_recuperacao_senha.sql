CREATE TABLE token_recuperacao_senha (
    id          UUID PRIMARY KEY,
    usuario_id  UUID NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now(),
    expira_em   TIMESTAMPTZ NOT NULL,
    usado_em    TIMESTAMPTZ
);

CREATE INDEX idx_token_recuperacao_senha_usuario_id ON token_recuperacao_senha (usuario_id);
