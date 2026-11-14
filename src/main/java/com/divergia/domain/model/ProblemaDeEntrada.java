package com.divergia.domain.model;

import java.util.Objects;

public record ProblemaDeEntrada(MotivoEntradaInvalida motivo, CampoEntrada campo, String mensagem) {

    public ProblemaDeEntrada {
        Objects.requireNonNull(motivo, "motivo não pode ser nulo");
        Objects.requireNonNull(campo, "campo não pode ser nulo");
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("mensagem não pode ser vazia");
        }
    }

    public static ProblemaDeEntrada de(MotivoEntradaInvalida motivo, CampoEntrada campo) {
        return new ProblemaDeEntrada(motivo, campo, String.format(motivo.modelo(), campo.rotulo()));
    }

    public static ProblemaDeEntrada textoLongo(CampoEntrada campo, int limite) {
        return new ProblemaDeEntrada(
                MotivoEntradaInvalida.TEXTO_LONGO, campo,
                String.format(MotivoEntradaInvalida.TEXTO_LONGO.modelo(), campo.rotulo(), limite));
    }

    public static ProblemaDeEntrada idiomasDiferentes(String idiomaOriginal, String idiomaEditado) {
        return new ProblemaDeEntrada(
                MotivoEntradaInvalida.IDIOMAS_DIFERENTES, CampoEntrada.AMBOS,
                String.format(MotivoEntradaInvalida.IDIOMAS_DIFERENTES.modelo(), idiomaOriginal, idiomaEditado));
    }
}
