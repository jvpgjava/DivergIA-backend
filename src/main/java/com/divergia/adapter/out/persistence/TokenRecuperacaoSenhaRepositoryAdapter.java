package com.divergia.adapter.out.persistence;

import com.divergia.application.port.out.TokenRecuperacaoSenhaRepositoryPort;
import com.divergia.domain.model.TokenRecuperacaoSenha;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class TokenRecuperacaoSenhaRepositoryAdapter implements TokenRecuperacaoSenhaRepositoryPort {

    private final TokenRecuperacaoSenhaJpaRepository repository;

    public TokenRecuperacaoSenhaRepositoryAdapter(TokenRecuperacaoSenhaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public TokenRecuperacaoSenha salvar(TokenRecuperacaoSenha token) {
        TokenRecuperacaoSenhaJpaEntity salvo = repository.save(TokenRecuperacaoSenhaMapper.toEntity(token));
        return TokenRecuperacaoSenhaMapper.toDomain(salvo);
    }

    @Override
    public Optional<TokenRecuperacaoSenha> buscarPorHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(TokenRecuperacaoSenhaMapper::toDomain);
    }

    @Override
    public void marcarComoUsado(UUID id) {
        TokenRecuperacaoSenhaJpaEntity existente = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Token de recuperação não encontrado: " + id));
        TokenRecuperacaoSenhaJpaEntity atualizado = new TokenRecuperacaoSenhaJpaEntity(
                existente.getId(),
                existente.getUsuarioId(),
                existente.getTokenHash(),
                existente.getCriadoEm(),
                existente.getExpiraEm(),
                Instant.now());
        repository.save(atualizado);
    }
}
