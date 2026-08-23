package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Token de uso único para redefinição de senha. Guarda apenas o hash do
 * token — o valor bruto só existe no momento em que é gerado e enviado por
 * e-mail, nunca é persistido.
 */
public record TokenRecuperacaoSenha(
        UUID id, UUID usuarioId, String tokenHash, Instant criadoEm, Instant expiraEm, Instant usadoEm) {

    public TokenRecuperacaoSenha {
        Objects.requireNonNull(id, "id não pode ser nulo");
        Objects.requireNonNull(usuarioId, "usuarioId não pode ser nulo");
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash não pode ser vazio");
        }
        Objects.requireNonNull(criadoEm, "criadoEm não pode ser nulo");
        Objects.requireNonNull(expiraEm, "expiraEm não pode ser nulo");
    }

    public boolean valido(Instant agora) {
        return usadoEm == null && agora.isBefore(expiraEm);
    }
}
