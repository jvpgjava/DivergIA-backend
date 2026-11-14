package com.divergia.application.usecase;

// Distinta de CredenciaisInvalidasException: senha atual errada com sessão válida não pode virar 401,
// senão o app trata como sessão expirada e desloga a pessoa.
public class SenhaAtualIncorretaException extends RuntimeException {

    public SenhaAtualIncorretaException() {
        super("Senha atual incorreta.");
    }
}
