package com.divergia.application.port.in;

import com.divergia.domain.model.Consentimento;

import java.util.UUID;

public interface ObterConsentimentoUseCase {

    /**
     * Devolve o consentimento vigente (mais recente) do usuário; se ele nunca
     * definiu nenhum, devolve um padrão privacy-by-default (tudo negado).
     */
    Consentimento obter(UUID usuarioId);
}
