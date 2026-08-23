package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Registro de revogação de um token de acesso (JWT) — permite logout real
 * mesmo com um esquema de autenticação stateless: toda requisição
 * autenticada verifica se o {@code jti} do token apresentado está aqui.
 */
public record TokenRevogado(String jti, Instant expiraEm) {

    public TokenRevogado {
        if (jti == null || jti.isBlank()) {
            throw new IllegalArgumentException("jti não pode ser vazio");
        }
        Objects.requireNonNull(expiraEm, "expiraEm não pode ser nulo");
    }
}
