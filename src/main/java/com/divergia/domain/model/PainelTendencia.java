package com.divergia.domain.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record PainelTendencia(
        long totalAnalises,
        long totalDerivas,
        double intensidadeMedia,
        Map<TipoDesvio, Long> derivasPorTipo,
        List<PontoTendencia> evolucaoMensal) {

    public PainelTendencia {
        Objects.requireNonNull(derivasPorTipo, "derivasPorTipo não pode ser nulo");
        Objects.requireNonNull(evolucaoMensal, "evolucaoMensal não pode ser nulo");
        derivasPorTipo = Map.copyOf(derivasPorTipo);
        evolucaoMensal = List.copyOf(evolucaoMensal);
    }
}
