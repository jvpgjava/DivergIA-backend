-- O token de acesso deixou de ter data de expiração: a sessão dura até o logout, que revoga o jti.
-- A coluna passa a registrar quando o token foi revogado. Os valores antigos (data em que o token
-- expiraria) ficam como estão: servem só de referência e a consulta é sempre por jti.
ALTER TABLE token_revogado RENAME COLUMN expira_em TO revogado_em;
ALTER INDEX idx_token_revogado_expira_em RENAME TO idx_token_revogado_revogado_em;
