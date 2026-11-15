package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.EstatisticasGlobais;
import com.divergia.domain.model.PontoDiarioGlobal;
import com.divergia.domain.model.TipoDesvio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record EstatisticasGlobaisResponse(
        long totalAnalises,
        long totalDerivas,
        Map<TipoDesvio, Long> derivasPorTipo,
        List<PontoDiario> serieDiaria,
        Instant geradoEm) {

    public record PontoDiario(LocalDate dia, long analises, Map<TipoDesvio, Long> derivasPorTipo, long totalDerivas) {

        static PontoDiario from(PontoDiarioGlobal ponto) {
            return new PontoDiario(ponto.dia(), ponto.analises(), ponto.derivasPorTipo(), ponto.totalDerivas());
        }
    }

    public static EstatisticasGlobaisResponse from(EstatisticasGlobais estatisticas) {
        return new EstatisticasGlobaisResponse(
                estatisticas.totalAnalises(),
                estatisticas.totalDerivas(),
                estatisticas.derivasPorTipo(),
                estatisticas.serieDiaria().stream().map(PontoDiario::from).toList(),
                estatisticas.geradoEm());
    }
}
