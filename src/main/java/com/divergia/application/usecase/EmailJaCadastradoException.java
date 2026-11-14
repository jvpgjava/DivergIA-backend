package com.divergia.application.usecase;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("Não foi possível usar esse e-mail.");
    }
}
