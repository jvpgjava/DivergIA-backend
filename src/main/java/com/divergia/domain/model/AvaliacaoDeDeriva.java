package com.divergia.domain.model;

import java.util.Objects;

/**
 * Julgamento bruto do LLM sobre um trecho onde a versão editada diverge do
 * texto original — ainda não persistido, e portanto sem {@code id} nem
 * vínculo com uma {@link Analise}. Um caso de uso posterior atribui esses
 * dois dados ao transformar isto em um {@link TrechoDeriva}.
 */
public record AvaliacaoDeDeriva(
        TipoDesvio tipoDesvio,
        String trechoOriginal,
        String trechoEditado,
        String explicacao,
        double intensidade) {

    public AvaliacaoDeDeriva {
        Objects.requireNonNull(tipoDesvio, "tipoDesvio não pode ser nulo");
        if (trechoOriginal == null || trechoOriginal.isBlank()) {
            throw new IllegalArgumentException("trechoOriginal não pode ser vazio");
        }
        if (trechoEditado == null || trechoEditado.isBlank()) {
            throw new IllegalArgumentException("trechoEditado não pode ser vazio");
        }
        if (explicacao == null || explicacao.isBlank()) {
            throw new IllegalArgumentException("explicacao não pode ser vazia");
        }
        if (intensidade < 0.0 || intensidade > 1.0) {
            throw new IllegalArgumentException("intensidade deve estar entre 0.0 e 1.0");
        }
    }
}
