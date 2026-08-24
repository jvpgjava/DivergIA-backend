package com.divergia.application.port.in;

import java.util.UUID;

public interface SugerirReescritaUseCase {

    /**
     * @throws com.divergia.application.usecase.TrechoDerivaNaoEncontradoException se o trecho não existir
     * @throws com.divergia.application.usecase.AcessoNaoAutorizadoException se o trecho pertencer a outro usuário
     */
    String sugerir(UUID usuarioId, UUID trechoDerivaId);
}
