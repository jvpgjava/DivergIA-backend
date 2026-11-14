package com.divergia.domain.model;

public enum CampoEntrada {
    ORIGINAL("texto original"),
    EDITADO("texto editado"),
    AMBOS("par de textos");

    private final String rotulo;

    CampoEntrada(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
