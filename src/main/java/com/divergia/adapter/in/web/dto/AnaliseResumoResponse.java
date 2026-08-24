package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.Analise;

import java.time.Instant;
import java.util.UUID;

public record AnaliseResumoResponse(UUID id, Instant criadoEm, boolean textoRetido) {

    public static AnaliseResumoResponse from(Analise analise) {
        return new AnaliseResumoResponse(analise.id(), analise.criadoEm(), analise.textoOriginal() != null);
    }
}
