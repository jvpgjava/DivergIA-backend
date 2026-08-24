package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.PainelTendencia;
import com.divergia.domain.model.TipoDesvio;

import java.util.List;
import java.util.Map;

public record PainelTendenciaResponse(
        long totalAnalises,
        long totalDerivas,
        double intensidadeMedia,
        Map<TipoDesvio, Long> derivasPorTipo,
        List<PontoTendenciaResponse> evolucaoMensal) {

    public static PainelTendenciaResponse from(PainelTendencia painel) {
        return new PainelTendenciaResponse(
                painel.totalAnalises(),
                painel.totalDerivas(),
                painel.intensidadeMedia(),
                painel.derivasPorTipo(),
                painel.evolucaoMensal().stream().map(PontoTendenciaResponse::from).toList());
    }
}
