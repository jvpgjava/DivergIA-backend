package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.TrechoDeriva;

import java.util.UUID;

public record TrechoDerivaResponse(
        UUID id,
        TipoDesvio tipoDesvio,
        String trechoOriginal,
        String trechoEditado,
        String explicacao,
        double intensidade) {

    public static TrechoDerivaResponse from(TrechoDeriva trecho) {
        return new TrechoDerivaResponse(
                trecho.id(),
                trecho.tipoDesvio(),
                trecho.trechoOriginal(),
                trecho.trechoEditado(),
                trecho.explicacao(),
                trecho.intensidade());
    }
}
