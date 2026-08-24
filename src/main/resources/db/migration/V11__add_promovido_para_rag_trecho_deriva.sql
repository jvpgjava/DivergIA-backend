ALTER TABLE trecho_deriva
    ADD COLUMN promovido_para_rag BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX idx_trecho_deriva_nao_promovidos ON trecho_deriva (promovido_para_rag) WHERE NOT promovido_para_rag;
