package com.divergia.application.usecase;

public class LoginGoogleIndisponivelException extends RuntimeException {

    public LoginGoogleIndisponivelException() {
        super("O login com Google não está disponível no momento.");
    }
}
