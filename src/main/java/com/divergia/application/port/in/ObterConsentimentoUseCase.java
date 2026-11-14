package com.divergia.application.port.in;

import com.divergia.domain.model.Consentimento;

import java.util.UUID;

public interface ObterConsentimentoUseCase {
    Consentimento obter(UUID usuarioId);
}
