-- Exclusão de conta (Fase 3) precisa remover em cascata os dados do próprio
-- usuário. trecho_deriva já cascateia via analise_id (Fase 1); faltava
-- analise e consentimento cascatearem via usuario_id.

ALTER TABLE analise
    DROP CONSTRAINT analise_usuario_id_fkey,
    ADD CONSTRAINT analise_usuario_id_fkey
        FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE;

ALTER TABLE consentimento
    DROP CONSTRAINT consentimento_usuario_id_fkey,
    ADD CONSTRAINT consentimento_usuario_id_fkey
        FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE;
