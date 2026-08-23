package com.divergia.adapter.out.persistence;

import com.divergia.domain.model.ExemploRag;

public class ExemploRagMapper {

    private ExemploRagMapper() {
    }

    public static ExemploRag toDomain(ExemploRagJpaEntity entity) {
        return new ExemploRag(
                entity.getId(),
                entity.getTextoOriginal(),
                entity.getTextoEditado(),
                entity.getTipoDesvio(),
                entity.getEmbedding(),
                entity.getOrigem(),
                entity.getCriadoEm());
    }

    public static ExemploRagJpaEntity toEntity(ExemploRag exemploRag) {
        return new ExemploRagJpaEntity(
                exemploRag.id(),
                exemploRag.textoOriginal(),
                exemploRag.textoEditado(),
                exemploRag.tipoDesvio(),
                exemploRag.embedding(),
                exemploRag.origem(),
                exemploRag.criadoEm());
    }
}
