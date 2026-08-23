package com.divergia.application.port.out;

import com.divergia.domain.model.TokenRecuperacaoSenha;

import java.util.Optional;
import java.util.UUID;

public interface TokenRecuperacaoSenhaRepositoryPort {

    TokenRecuperacaoSenha salvar(TokenRecuperacaoSenha token);

    Optional<TokenRecuperacaoSenha> buscarPorHash(String tokenHash);

    void marcarComoUsado(UUID id);
}
