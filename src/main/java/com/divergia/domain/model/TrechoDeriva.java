package com.divergia.domain.model;

import java.util.Objects;
import java.util.UUID;

public record TrechoDeriva(
        UUID id,
        UUID analiseId,
        String trechoOriginal,
        String trechoEditado,
        TipoDesvio tipoDesvio,
        String explicacao,
        double intensidade,
        boolean promovidoParaRag,
        String sugestaoAceita) {

    public TrechoDeriva {
        Objects.requireNonNull(id, "id não pode ser nulo");
        Objects.requireNonNull(analiseId, "analiseId não pode ser nulo");
        if (trechoOriginal == null || trechoOriginal.isBlank()) {
            throw new IllegalArgumentException("trechoOriginal não pode ser vazio");
        }
        if (trechoEditado == null || trechoEditado.isBlank()) {
            throw new IllegalArgumentException("trechoEditado não pode ser vazio");
        }
        Objects.requireNonNull(tipoDesvio, "tipoDesvio não pode ser nulo");
        if (explicacao == null || explicacao.isBlank()) {
            throw new IllegalArgumentException("explicacao não pode ser vazia");
        }
        if (intensidade < 0.0 || intensidade > 1.0) {
            throw new IllegalArgumentException("intensidade deve estar entre 0.0 e 1.0");
        }
    }

    public TrechoDeriva(
            UUID id,
            UUID analiseId,
            String trechoOriginal,
            String trechoEditado,
            TipoDesvio tipoDesvio,
            String explicacao,
            double intensidade,
            boolean promovidoParaRag) {
        this(
                id, analiseId, trechoOriginal, trechoEditado, tipoDesvio, explicacao, intensidade,
                promovidoParaRag, null);
    }

    public TrechoDeriva comSugestaoAceita(String texto) {
        return new TrechoDeriva(
                id, analiseId, trechoOriginal, trechoEditado, tipoDesvio, explicacao, intensidade,
                promovidoParaRag, texto);
    }
}
