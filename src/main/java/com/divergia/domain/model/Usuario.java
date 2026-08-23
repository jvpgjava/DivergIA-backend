package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Usuario(UUID id, String nome, String email, String senhaHash, Instant criadoEm) {

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
}
