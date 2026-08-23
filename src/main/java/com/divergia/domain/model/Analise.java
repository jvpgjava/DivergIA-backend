package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Uma análise comparativa entre um texto original e sua versão editada.
 *
 * <p>{@code textoOriginal} e {@code textoEditado} são nulos quando
 * {@code manterHistorico} é falso — ver {@link com.divergia.domain.service.PoliticaRetencaoDeTexto},
 * que é a única forma autorizada de aplicar essa regra.
 */
public record Analise(
        UUID id,
        UUID usuarioId,
        String textoOriginal,
        String textoEditado,
        boolean manterHistorico,
        Instant criadoEm) {

    public Analise {
        Objects.requireNonNull(id, "id não pode ser nulo");
        Objects.requireNonNull(usuarioId, "usuarioId não pode ser nulo");
        Objects.requireNonNull(criadoEm, "criadoEm não pode ser nulo");
    }
}
