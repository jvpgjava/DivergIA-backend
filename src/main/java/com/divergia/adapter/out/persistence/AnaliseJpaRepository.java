package com.divergia.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnaliseJpaRepository extends JpaRepository<AnaliseJpaEntity, UUID> {
}
