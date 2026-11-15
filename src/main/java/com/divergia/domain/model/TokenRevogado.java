package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;

public record TokenRevogado(String jti, Instant revogadoEm) {

    public TokenRevogado {
        if (jti == null || jti.isBlank()) {
            throw new IllegalArgumentException("jti não pode ser vazio");
        }
        Objects.requireNonNull(revogadoEm, "revogadoEm não pode ser nulo");
    }
}
