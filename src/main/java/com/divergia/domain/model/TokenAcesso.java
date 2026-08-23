package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Token de acesso (JWT) emitido para um usuário autenticado — representa
 * tanto o token recém-gerado no login quanto o resultado de validar um
 * token recebido numa requisição.
 */
public record TokenAcesso(String valor, String jti, UUID usuarioId, Instant expiraEm) {

    public TokenAcesso {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("valor não pode ser vazio");
        }
        if (jti == null || jti.isBlank()) {
            throw new IllegalArgumentException("jti não pode ser vazio");
        }
        Objects.requireNonNull(usuarioId, "usuarioId não pode ser nulo");
        Objects.requireNonNull(expiraEm, "expiraEm não pode ser nulo");
    }
}
