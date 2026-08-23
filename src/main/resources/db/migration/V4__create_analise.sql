CREATE TABLE analise (
    id                UUID PRIMARY KEY,
    usuario_id        UUID NOT NULL REFERENCES usuario (id),
    texto_original    TEXT,
    texto_editado     TEXT,
    manter_historico  BOOLEAN NOT NULL,
    criado_em         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_analise_usuario_id ON analise (usuario_id);
