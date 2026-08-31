package com.divergia.application.port.in;

import com.divergia.domain.model.Usuario;

import java.util.UUID;

public interface AlterarEmailUseCase {

    /**
     * @throws com.divergia.application.usecase.CredenciaisInvalidasException se a senha estiver incorreta
     * @throws com.divergia.application.usecase.EmailJaCadastradoException se o novo e-mail já estiver em uso
     */
    Usuario alterar(UUID usuarioId, String novoEmail, String senhaAtual);
}
