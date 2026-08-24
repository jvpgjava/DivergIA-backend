package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.Consentimento;

import java.time.Instant;

public record ConsentimentoResponse(boolean manterHistorico, boolean contribuirParaRag, Instant concedidoEm) {

    public static ConsentimentoResponse from(Consentimento consentimento) {
        return new ConsentimentoResponse(
                consentimento.manterHistorico(), consentimento.contribuirParaRag(), consentimento.concedidoEm());
    }
}
