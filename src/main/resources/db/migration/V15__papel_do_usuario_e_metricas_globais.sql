-- Papel do usuário: todo mundo é USUARIO; DEV libera o painel de métricas globais.
-- O papel só é alterado por script de administração direto no banco, nunca pela API.
ALTER TABLE usuario
    ADD COLUMN papel VARCHAR(20) NOT NULL DEFAULT 'USUARIO'
        CONSTRAINT ck_usuario_papel CHECK (papel IN ('USUARIO', 'DEV'));

-- Contadores globais por dia. Ficam separados de analise/trecho_deriva de propósito: o usuário
-- pode apagar o histórico ou a conta, e o total de análises feitas não pode diminuir por isso.
-- Guardam só números agregados, nenhum texto nem identificador de usuário.
CREATE TABLE metrica_analise_diaria (
    dia                 DATE PRIMARY KEY,
    analises            BIGINT NOT NULL DEFAULT 0 CHECK (analises >= 0),
    derivas_sentido     BIGINT NOT NULL DEFAULT 0 CHECK (derivas_sentido >= 0),
    derivas_posicao     BIGINT NOT NULL DEFAULT 0 CHECK (derivas_posicao >= 0),
    derivas_intensidade BIGINT NOT NULL DEFAULT 0 CHECK (derivas_intensidade >= 0)
);

-- Carga inicial com o que já existe. Análises sem histórico guardado não têm trechos no banco,
-- então as divergências do passado só cobrem as análises que mantiveram histórico.
INSERT INTO metrica_analise_diaria (dia, analises, derivas_sentido, derivas_posicao, derivas_intensidade)
SELECT a.dia,
       a.total,
       COALESCE(d.sentido, 0),
       COALESCE(d.posicao, 0),
       COALESCE(d.intensidade, 0)
FROM (SELECT (criado_em AT TIME ZONE 'America/Sao_Paulo')::date AS dia, count(*) AS total
      FROM analise
      GROUP BY 1) a
         LEFT JOIN (SELECT (an.criado_em AT TIME ZONE 'America/Sao_Paulo')::date AS dia,
                           count(*) FILTER (WHERE t.tipo_desvio = 'SENTIDO')     AS sentido,
                           count(*) FILTER (WHERE t.tipo_desvio = 'POSICAO')     AS posicao,
                           count(*) FILTER (WHERE t.tipo_desvio = 'INTENSIDADE') AS intensidade
                    FROM trecho_deriva t
                             JOIN analise an ON an.id = t.analise_id
                    GROUP BY 1) d ON d.dia = a.dia;
