package com.divergia.adapter.out.persistence;

import com.divergia.domain.model.Consentimento;

public class ConsentimentoMapper {

    private ConsentimentoMapper() {
    }

    public static Consentimento toDomain(ConsentimentoJpaEntity entity) {
        return new Consentimento(
                entity.getId(),
                entity.getUsuarioId(),
                entity.isManterHistorico(),
                entity.getConcedidoEm());
    }

    public static ConsentimentoJpaEntity toEntity(Consentimento consentimento) {
        return new ConsentimentoJpaEntity(
                consentimento.id(),
                consentimento.usuarioId(),
                consentimento.manterHistorico(),
                consentimento.concedidoEm());
    }
}
