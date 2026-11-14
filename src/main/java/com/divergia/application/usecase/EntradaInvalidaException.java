package com.divergia.application.usecase;

import com.divergia.domain.model.ProblemaDeEntrada;

public class EntradaInvalidaException extends RuntimeException {

    private final transient ProblemaDeEntrada problema;

    public EntradaInvalidaException(ProblemaDeEntrada problema) {
        super(problema.mensagem());
        this.problema = problema;
    }

    public ProblemaDeEntrada problema() {
        return problema;
    }
}
