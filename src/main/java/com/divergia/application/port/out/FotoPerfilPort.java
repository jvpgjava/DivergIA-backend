package com.divergia.application.port.out;

import java.util.UUID;

public interface FotoPerfilPort {
    String salvar(UUID usuarioId, byte[] conteudo, String extensao);
}
