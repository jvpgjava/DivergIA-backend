package com.divergia.application.port.in;

import com.divergia.domain.model.Usuario;

import java.util.UUID;

public interface ObterUsuarioLogadoUseCase {
    Usuario obter(UUID usuarioId);
}
