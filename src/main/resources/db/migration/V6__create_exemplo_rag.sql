CREATE TABLE exemplo_rag (
    id               UUID PRIMARY KEY,
    texto_original   TEXT NOT NULL,
    texto_editado    TEXT NOT NULL,
    tipo_desvio      VARCHAR(20) NOT NULL CHECK (tipo_desvio IN ('SENTIDO', 'POSICAO', 'INTENSIDADE')),
    embedding        VECTOR(768) NOT NULL,
    origem           VARCHAR(30) NOT NULL CHECK (origem IN ('ESTUDO_OXFORD_POTSDAM', 'PROMOVIDO_DE_ANALISE')),
    criado_em        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_exemplo_rag_embedding ON exemplo_rag USING hnsw (embedding vector_cosine_ops);
