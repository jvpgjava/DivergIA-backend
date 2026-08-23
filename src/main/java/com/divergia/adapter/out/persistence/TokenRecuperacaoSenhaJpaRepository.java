package com.divergia.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenRecuperacaoSenhaJpaRepository extends JpaRepository<TokenRecuperacaoSenhaJpaEntity, UUID> {

    Optional<TokenRecuperacaoSenhaJpaEntity> findByTokenHash(String tokenHash);
}
