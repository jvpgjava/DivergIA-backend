package com.divergia.application.usecase;

public class TrechoDerivaNaoEncontradoException extends RuntimeException {

    public TrechoDerivaNaoEncontradoException() {
        super("Trecho de deriva não encontrado");
    }
}
