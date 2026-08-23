CREATE TABLE trecho_deriva (
    id                UUID PRIMARY KEY,
    analise_id        UUID NOT NULL REFERENCES analise (id) ON DELETE CASCADE,
    trecho_original   TEXT NOT NULL,
    trecho_editado    TEXT NOT NULL,
    tipo_desvio       VARCHAR(20) NOT NULL CHECK (tipo_desvio IN ('SENTIDO', 'POSICAO', 'INTENSIDADE')),
    explicacao        TEXT NOT NULL,
    intensidade       DOUBLE PRECISION NOT NULL CHECK (intensidade >= 0.0 AND intensidade <= 1.0)
);

CREATE INDEX idx_trecho_deriva_analise_id ON trecho_deriva (analise_id);
