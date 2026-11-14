package com.divergia.domain.model;

import java.util.Objects;

public record IdentidadeGoogle(String googleId, String email, String nome) {

    public IdentidadeGoogle {
        Objects.requireNonNull(googleId, "googleId não pode ser nulo");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email não pode ser vazio");
        }
    }
}
