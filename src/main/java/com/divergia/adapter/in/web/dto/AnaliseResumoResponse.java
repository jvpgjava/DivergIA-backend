package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.ResultadoAnalise;
import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.TrechoDeriva;

import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;

public record AnaliseResumoResponse(
        UUID id,
        Instant criadoEm,
        boolean textoRetido,
        Integer pontuacaoIntensidade,
        TipoDesvio tipoDesvioPrincipal,
        String textoPreview) {

    private static final int TAMANHO_MAXIMO_PREVIEW = 140;

    public static AnaliseResumoResponse from(ResultadoAnalise resultado) {
        var analise = resultado.analise();
        TrechoDeriva maisIntenso = resultado.trechosDeDeriva().stream()
                .max(Comparator.comparingDouble(TrechoDeriva::intensidade))
                .orElse(null);

        return new AnaliseResumoResponse(
                analise.id(),
                analise.criadoEm(),
                analise.textoOriginal() != null,
                maisIntenso == null ? null : (int) Math.round(maisIntenso.intensidade() * 100),
                maisIntenso == null ? null : maisIntenso.tipoDesvio(),
                truncar(analise.textoOriginal()));
    }

    private static String truncar(String texto) {
        if (texto == null) return null;
        if (texto.length() <= TAMANHO_MAXIMO_PREVIEW) return texto;
        return texto.substring(0, TAMANHO_MAXIMO_PREVIEW).trim() + "…";
    }
}
