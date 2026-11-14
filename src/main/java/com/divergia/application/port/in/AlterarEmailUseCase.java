package com.divergia.application.port.in;

import com.divergia.domain.model.Usuario;

import java.util.UUID;

public interface AlterarEmailUseCase {
    Usuario alterar(UUID usuarioId, String novoEmail, String senhaAtual);
}
