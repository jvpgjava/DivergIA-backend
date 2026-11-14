package com.divergia.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

    Optional<UsuarioJpaEntity> findByEmail(String email);

    // Ignora a caixa: contas antigas guardadas com maiúsculas continuam sendo encontradas.
    Optional<UsuarioJpaEntity> findFirstByEmailIgnoreCaseOrderByCriadoEmAsc(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);
}
