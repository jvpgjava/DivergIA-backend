package com.divergia.domain.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Painel de tendência pessoal (RF23–RF24): visão agregada de como as
 * análises de um usuário evoluíram — quantidade, distribuição por tipo de
 * desvio, intensidade média, e evolução mês a mês.
 */
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
