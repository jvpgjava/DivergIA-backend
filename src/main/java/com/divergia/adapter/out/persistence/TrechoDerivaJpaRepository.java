package com.divergia.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TrechoDerivaJpaRepository extends JpaRepository<TrechoDerivaJpaEntity, UUID> {

    List<TrechoDerivaJpaEntity> findByAnaliseId(UUID analiseId);

    @Query("SELECT t FROM TrechoDerivaJpaEntity t "
            + "WHERE t.analiseId IN (SELECT a.id FROM AnaliseJpaEntity a WHERE a.usuarioId = :usuarioId)")
    List<TrechoDerivaJpaEntity> findByUsuarioId(@Param("usuarioId") UUID usuarioId);

    List<TrechoDerivaJpaEntity> findByPromovidoParaRagFalse();
}
