-- Um e-mail = uma conta, ignorando maiúsculas. Se já existirem contas duplicadas por caixa
-- (ex.: Ana@x.com e ana@x.com), o índice NÃO é criado para não derrubar o deploy: unifique as
-- contas e crie o índice depois (CREATE UNIQUE INDEX ux_usuario_email_lower ON usuario (lower(email))).
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM usuario GROUP BY lower(email) HAVING count(*) > 1) THEN
        RAISE NOTICE 'Há e-mails duplicados ignorando maiúsculas; índice único não criado.';
    ELSE
        CREATE UNIQUE INDEX IF NOT EXISTS ux_usuario_email_lower ON usuario (lower(email));
    END IF;
END
$$;
