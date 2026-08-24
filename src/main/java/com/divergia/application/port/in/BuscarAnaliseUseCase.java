package com.divergia.application.port.in;

import com.divergia.domain.model.ResultadoAnalise;

import java.util.UUID;

public interface BuscarAnaliseUseCase {

    /**
     * @throws com.divergia.application.usecase.AnaliseNaoEncontradaException se a análise não existir
     * @throws com.divergia.application.usecase.AcessoNaoAutorizadoException se a análise pertencer a outro usuário
     */
    ResultadoAnalise buscar(UUID usuarioId, UUID analiseId);
}
