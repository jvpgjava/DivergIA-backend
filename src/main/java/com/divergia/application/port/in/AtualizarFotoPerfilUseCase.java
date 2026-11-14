package com.divergia.application.port.in;

import java.util.UUID;

public interface AtualizarFotoPerfilUseCase {
    String atualizar(UUID usuarioId, byte[] conteudo, String extensao);
}
