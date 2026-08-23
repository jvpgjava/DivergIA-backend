CREATE TABLE token_revogado (
    jti        VARCHAR(36) PRIMARY KEY,
    expira_em  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_token_revogado_expira_em ON token_revogado (expira_em);
