package com.divergia.application.usecase;

public class AnaliseNaoEncontradaException extends RuntimeException {

    public AnaliseNaoEncontradaException() {
        super("Análise não encontrada");
    }
}
