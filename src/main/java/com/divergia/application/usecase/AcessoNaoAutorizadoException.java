package com.divergia.application.usecase;

/**
 * Usuário autenticado tentando acessar um recurso que pertence a outro usuário.
 */
public class AcessoNaoAutorizadoException extends RuntimeException {

    public AcessoNaoAutorizadoException() {
        super("Você não tem acesso a este recurso");
    }
}
