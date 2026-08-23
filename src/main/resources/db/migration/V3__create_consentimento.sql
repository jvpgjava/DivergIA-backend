CREATE TABLE consentimento (
    id                UUID PRIMARY KEY,
    usuario_id        UUID NOT NULL REFERENCES usuario (id),
    manter_historico  BOOLEAN NOT NULL,
    concedido_em      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_consentimento_usuario_id ON consentimento (usuario_id);
