package com.divergia.domain.model;

import java.util.Objects;
import java.util.UUID;

public record TokenAcesso(String valor, String jti, UUID usuarioId) {

    public TokenAcesso {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("valor não pode ser vazio");
        }
        if (jti == null || jti.isBlank()) {
            throw new IllegalArgumentException("jti não pode ser vazio");
        }
        Objects.requireNonNull(usuarioId, "usuarioId não pode ser nulo");
    }
}
