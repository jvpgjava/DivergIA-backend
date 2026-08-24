package com.divergia.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Resultado de uma análise: a {@link Analise} em si e os trechos de deriva
 * identificados. Os trechos podem existir aqui mesmo quando não persistidos
 * (usuário não consentiu em manter histórico) — o resultado é sempre
 * devolvido na hora, só a persistência além do processamento é que respeita
 * a regra de retenção.
 */
public record ResultadoAnalise(Analise analise, List<TrechoDeriva> trechosDeDeriva) {

    public ResultadoAnalise {
        Objects.requireNonNull(analise, "analise não pode ser nula");
        Objects.requireNonNull(trechosDeDeriva, "trechosDeDeriva não pode ser nulo");
        trechosDeDeriva = List.copyOf(trechosDeDeriva);
    }
}
