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

    public static Usuario novo(String nome, String email, String senhaHash) {
        return new Usuario(UUID.randomUUID(), nome, email, senhaHash, Instant.now(), null);
    }

    public Usuario comEmail(String novoEmail) {
        return new Usuario(id, nome, novoEmail, senhaHash, criadoEm, fotoUrl);
    }

    public Usuario comSenhaHash(String novoSenhaHash) {
        return new Usuario(id, nome, email, novoSenhaHash, criadoEm, fotoUrl);
    }

    public Usuario comFotoUrl(String novaFotoUrl) {
        return new Usuario(id, nome, email, senhaHash, criadoEm, novaFotoUrl);
    }
}
