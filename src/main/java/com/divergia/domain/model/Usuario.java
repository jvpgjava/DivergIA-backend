package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Usuario(
        UUID id, String nome, String email, String senhaHash, Instant criadoEm, String fotoUrl, Papel papel) {

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
        Objects.requireNonNull(papel, "papel não pode ser nulo");
    }

    public Usuario(UUID id, String nome, String email, String senhaHash, Instant criadoEm, String fotoUrl) {
        this(id, nome, email, senhaHash, criadoEm, fotoUrl, Papel.USUARIO);
    }

    public static Usuario novo(String nome, String email, String senhaHash) {
        return new Usuario(UUID.randomUUID(), nome, email, senhaHash, Instant.now(), null, Papel.USUARIO);
    }

    public boolean ehDev() {
        return papel == Papel.DEV;
    }

    public Usuario comEmail(String novoEmail) {
        return new Usuario(id, nome, novoEmail, senhaHash, criadoEm, fotoUrl, papel);
    }

    public Usuario comSenhaHash(String novoSenhaHash) {
        return new Usuario(id, nome, email, novoSenhaHash, criadoEm, fotoUrl, papel);
    }

    public Usuario comFotoUrl(String novaFotoUrl) {
        return new Usuario(id, nome, email, senhaHash, criadoEm, novaFotoUrl, papel);
    }
}
