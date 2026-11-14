package com.divergia.application.usecase;

public class SugestaoIndisponivelException extends RuntimeException {

    public SugestaoIndisponivelException() {
        super("Não conseguimos gerar uma reescrita fiel para este trecho agora. Tente novamente em instantes.");
    }
}
