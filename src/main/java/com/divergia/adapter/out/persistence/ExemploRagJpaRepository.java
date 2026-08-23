package com.divergia.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExemploRagJpaRepository extends JpaRepository<ExemploRagJpaEntity, UUID> {

    @Query(
            value = "SELECT * FROM exemplo_rag ORDER BY embedding <=> CAST(:embedding AS vector) LIMIT :quantidade",
            nativeQuery = true)
    List<ExemploRagJpaEntity> buscarMaisSimilares(
            @Param("embedding") String embedding, @Param("quantidade") int quantidade);
}
