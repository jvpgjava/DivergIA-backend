package com.divergia.application.port.in;

import com.divergia.domain.model.Consentimento;

import java.util.UUID;

public interface AtualizarConsentimentoUseCase {

    Consentimento atualizar(UUID usuarioId, boolean manterHistorico, boolean contribuirParaRag);
}
