package com.divergia.application.port.in;

public interface RedefinirSenhaUseCase {

    /**
     * @throws com.divergia.application.usecase.TokenInvalidoOuExpiradoException se o token não for válido
     */
    void redefinir(String tokenBruto, String novaSenha);
}
