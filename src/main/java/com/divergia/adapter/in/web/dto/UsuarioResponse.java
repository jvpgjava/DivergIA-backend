package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.Usuario;

import java.time.Instant;
import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email, Instant criadoEm, String fotoUrl) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.id(), usuario.nome(), usuario.email(), usuario.criadoEm(), usuario.fotoUrl());
    }
}
