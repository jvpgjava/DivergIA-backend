package com.divergia.application.usecase;

public class TokenInvalidoOuExpiradoException extends RuntimeException {

    public TokenInvalidoOuExpiradoException() {
        super("Token de recuperação de senha inválido ou expirado");
    }
}
