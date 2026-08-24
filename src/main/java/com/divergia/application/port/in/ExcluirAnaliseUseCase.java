package com.divergia.application.port.in;

import java.util.UUID;

public interface ExcluirAnaliseUseCase {

    /**
     * @throws com.divergia.application.usecase.AnaliseNaoEncontradaException se a análise não existir
     * @throws com.divergia.application.usecase.AcessoNaoAutorizadoException se a análise pertencer a outro usuário
     */
    void excluir(UUID usuarioId, UUID analiseId);
}
