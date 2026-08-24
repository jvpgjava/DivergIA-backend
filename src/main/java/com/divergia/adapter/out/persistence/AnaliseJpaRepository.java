package com.divergia.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnaliseJpaRepository extends JpaRepository<AnaliseJpaEntity, UUID> {

    List<AnaliseJpaEntity> findByUsuarioId(UUID usuarioId);

    void deleteByUsuarioId(UUID usuarioId);
}
