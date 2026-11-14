package com.divergia.adapter.out.persistence;

import com.divergia.domain.model.TrechoDeriva;

public class TrechoDerivaMapper {

    private TrechoDerivaMapper() {
    }

    public static TrechoDeriva toDomain(TrechoDerivaJpaEntity entity) {
        return new TrechoDeriva(
                entity.getId(),
                entity.getAnaliseId(),
                entity.getTrechoOriginal(),
                entity.getTrechoEditado(),
                entity.getTipoDesvio(),
                entity.getExplicacao(),
                entity.getIntensidade(),
                entity.isPromovidoParaRag(),
                entity.getSugestaoAceita());
    }

    public static TrechoDerivaJpaEntity toEntity(TrechoDeriva trechoDeriva) {
        return new TrechoDerivaJpaEntity(
                trechoDeriva.id(),
                trechoDeriva.analiseId(),
                trechoDeriva.trechoOriginal(),
                trechoDeriva.trechoEditado(),
                trechoDeriva.tipoDesvio(),
                trechoDeriva.explicacao(),
                trechoDeriva.intensidade(),
                trechoDeriva.promovidoParaRag(),
                trechoDeriva.sugestaoAceita());
    }
}
