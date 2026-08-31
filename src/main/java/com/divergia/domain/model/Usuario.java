package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Usuario(UUID id, String nome, String email, String senhaHash, Instant criadoEm, String fotoUrl) {

    public Usuario {
        Objects.requireNonNull(id, "id não pode ser nulo");
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome não pode ser vazio");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email não pode ser vazio");
        }
        Objects.requireNonNull(senhaHash, "senhaHash não pode ser nulo");
        Objects.requireNonNull(criadoEm, "criadoEm não pode ser nulo");
    }

    /** @deprecated use o construtor com {@code fotoUrl} — mantido para não quebrar quem ainda não define foto. */
    @Deprecated
    public Usuario(UUID id, String nome, String email, String senhaHash, Instant criadoEm) {
        this(id, nome, email, senhaHash, criadoEm, null);
    }
}
