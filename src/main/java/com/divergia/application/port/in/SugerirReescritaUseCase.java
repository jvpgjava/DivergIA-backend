package com.divergia.application.port.in;

import java.util.List;
import java.util.UUID;

public interface SugerirReescritaUseCase {

    /**
     * @return 3 sugestões de reescrita alternativas
     * @throws com.divergia.application.usecase.TrechoDerivaNaoEncontradoException se o trecho não existir
     * @throws com.divergia.application.usecase.AcessoNaoAutorizadoException se o trecho pertencer a outro usuário
     */
    List<String> sugerir(UUID usuarioId, UUID trechoDerivaId);
}
