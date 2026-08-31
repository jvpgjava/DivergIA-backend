package com.divergia.application.port.in;

import java.util.UUID;

public interface AlterarSenhaUseCase {

    /**
     * @throws com.divergia.application.usecase.CredenciaisInvalidasException se a senha atual estiver incorreta
     */
    void alterar(UUID usuarioId, String senhaAtual, String novaSenha);
}
