package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.ResultadoAnalise;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResultadoAnaliseResponse(UUID analiseId, Instant criadoEm, List<TrechoDerivaResponse> trechos) {

    public static ResultadoAnaliseResponse from(ResultadoAnalise resultado) {
        return new ResultadoAnaliseResponse(
                resultado.analise().id(),
                resultado.analise().criadoEm(),
                resultado.trechosDeDeriva().stream().map(TrechoDerivaResponse::from).toList());
    }
}
