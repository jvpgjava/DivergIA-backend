package com.divergia.application.port.in;

import com.divergia.domain.model.Usuario;

import java.util.UUID;

public interface ObterUsuarioLogadoUseCase {

    /**
     * @throws com.divergia.application.usecase.UsuarioNaoEncontradoException se o usuário do token não existir mais
     */
    Usuario obter(UUID usuarioId);
}
