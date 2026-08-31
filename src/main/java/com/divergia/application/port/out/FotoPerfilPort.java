package com.divergia.application.port.out;

import java.util.UUID;

/**
 * Onde a foto de perfil do usuário é guardada — hoje um adapter que grava no
 * disco da própria VPS (ver {@code FotoPerfilDiscoAdapter}).
 */
public interface FotoPerfilPort {

    /**
     * @return URL pública pela qual a foto salva pode ser acessada
     */
    String salvar(UUID usuarioId, byte[] conteudo, String extensao);
}
