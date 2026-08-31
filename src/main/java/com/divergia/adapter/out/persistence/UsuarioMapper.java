package com.divergia.adapter.out.persistence;

import com.divergia.domain.model.Usuario;

public class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static Usuario toDomain(UsuarioJpaEntity entity) {
        return new Usuario(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getSenhaHash(),
                entity.getCriadoEm(),
                entity.getFotoUrl());
    }

    public static UsuarioJpaEntity toEntity(Usuario usuario) {
        return new UsuarioJpaEntity(
                usuario.id(),
                usuario.nome(),
                usuario.email(),
                usuario.senhaHash(),
                usuario.criadoEm(),
                usuario.fotoUrl());
    }
}
