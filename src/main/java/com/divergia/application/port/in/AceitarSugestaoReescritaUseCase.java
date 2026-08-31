package com.divergia.application.port.in;

import java.util.UUID;

public interface AceitarSugestaoReescritaUseCase {

    /**
     * Persiste qual das sugestões de reescrita geradas o usuário escolheu
     * aceitar para um trecho de deriva — fica visível depois junto do
     * resultado da análise.
     *
     * @throws com.divergia.application.usecase.TrechoDerivaNaoEncontradoException se o trecho não existir
     * @throws com.divergia.application.usecase.AcessoNaoAutorizadoException se o trecho pertencer a outro usuário
     */
    void aceitar(UUID usuarioId, UUID trechoDerivaId, String textoEscolhido);
}
