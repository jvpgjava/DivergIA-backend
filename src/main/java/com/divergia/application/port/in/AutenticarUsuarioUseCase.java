package com.divergia.application.port.in;

import com.divergia.domain.model.TokenAcesso;

public interface AutenticarUsuarioUseCase {

    /**
     * @throws com.divergia.application.usecase.CredenciaisInvalidasException se e-mail ou senha não conferirem
     */
    TokenAcesso autenticar(String email, String senha);
}
