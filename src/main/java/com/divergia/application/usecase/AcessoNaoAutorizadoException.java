package com.divergia.application.usecase;

public class AcessoNaoAutorizadoException extends RuntimeException {

    public AcessoNaoAutorizadoException() {
        super("Você não tem acesso a este recurso");
    }
}
