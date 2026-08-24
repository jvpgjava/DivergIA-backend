package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.PontoTendencia;

public record PontoTendenciaResponse(String mes, long quantidadeAnalises, long quantidadeDerivas, double intensidadeMedia) {

    public static PontoTendenciaResponse from(PontoTendencia ponto) {
        return new PontoTendenciaResponse(
                ponto.mes().toString(), ponto.quantidadeAnalises(), ponto.quantidadeDerivas(), ponto.intensidadeMedia());
    }
}
