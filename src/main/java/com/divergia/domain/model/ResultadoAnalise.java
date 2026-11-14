package com.divergia.domain.model;

import java.util.List;
import java.util.Objects;

public record ResultadoAnalise(Analise analise, List<TrechoDeriva> trechosDeDeriva) {

    public ResultadoAnalise {
        Objects.requireNonNull(analise, "analise não pode ser nula");
        Objects.requireNonNull(trechosDeDeriva, "trechosDeDeriva não pode ser nulo");
        trechosDeDeriva = List.copyOf(trechosDeDeriva);
    }
}
