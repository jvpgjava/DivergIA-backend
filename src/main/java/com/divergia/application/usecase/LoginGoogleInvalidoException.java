package com.divergia.application.usecase;

public class LoginGoogleInvalidoException extends RuntimeException {

    public LoginGoogleInvalidoException() {
        super("Não foi possível entrar com o Google. Tente novamente.");
    }
}
