package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;

public record TokenRevogado(String jti, Instant expiraEm) {

    public TokenRevogado {
        if (jti == null || jti.isBlank()) {
            throw new IllegalArgumentException("jti não pode ser vazio");
        }
        Objects.requireNonNull(expiraEm, "expiraEm não pode ser nulo");
    }
}
