package com.divergia.adapter.out.persistence;

import com.divergia.domain.model.TokenRecuperacaoSenha;

public class TokenRecuperacaoSenhaMapper {

    private TokenRecuperacaoSenhaMapper() {
    }

    public static TokenRecuperacaoSenha toDomain(TokenRecuperacaoSenhaJpaEntity entity) {
        return new TokenRecuperacaoSenha(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getTokenHash(),
                entity.getCriadoEm(),
                entity.getExpiraEm(),
                entity.getUsadoEm());
    }

    public static TokenRecuperacaoSenhaJpaEntity toEntity(TokenRecuperacaoSenha token) {
        return new TokenRecuperacaoSenhaJpaEntity(
                token.id(),
                token.usuarioId(),
                token.tokenHash(),
                token.criadoEm(),
                token.expiraEm(),
                token.usadoEm());
    }
}
