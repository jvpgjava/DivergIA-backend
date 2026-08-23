package com.divergia.adapter.out.persistence;

import com.divergia.domain.model.Analise;

public class AnaliseMapper {

    private AnaliseMapper() {
    }

    public static Analise toDomain(AnaliseJpaEntity entity) {
        return new Analise(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getTextoOriginal(),
                entity.getTextoEditado(),
                entity.isManterHistorico(),
                entity.getCriadoEm());
    }

    public static AnaliseJpaEntity toEntity(Analise analise) {
        return new AnaliseJpaEntity(
                analise.id(),
                analise.usuarioId(),
                analise.textoOriginal(),
                analise.textoEditado(),
                analise.manterHistorico(),
                analise.criadoEm());
    }
}
