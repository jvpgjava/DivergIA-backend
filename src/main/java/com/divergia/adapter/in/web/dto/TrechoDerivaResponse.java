package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.TrechoDeriva;

public record TrechoDerivaResponse(
        TipoDesvio tipoDesvio, String trechoOriginal, String trechoEditado, String explicacao, double intensidade) {

    public static TrechoDerivaResponse from(TrechoDeriva trecho) {
        return new TrechoDerivaResponse(
                trecho.tipoDesvio(),
                trecho.trechoOriginal(),
                trecho.trechoEditado(),
                trecho.explicacao(),
                trecho.intensidade());
    }
}
