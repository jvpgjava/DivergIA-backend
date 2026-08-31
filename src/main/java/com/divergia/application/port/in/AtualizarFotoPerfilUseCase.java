package com.divergia.application.port.in;

import java.util.UUID;

public interface AtualizarFotoPerfilUseCase {

    /**
     * @return URL pública da foto salva
     */
    String atualizar(UUID usuarioId, byte[] conteudo, String extensao);
}
