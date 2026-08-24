package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Preferências de privacidade de um usuário, registradas como um evento
 * pontual (cada mudança gera um novo registro — o mais recente por
 * {@code usuarioId} é o vigente):
 *
 * <ul>
 *   <li>{@code manterHistorico}: se o texto bruto de suas análises é
 *       mantido além do necessário para o processamento;</li>
 *   <li>{@code contribuirParaRag}: se trechos de suas análises podem ser
 *       promovidos para a base de exemplos compartilhada (RAG), usada como
 *       referência nas análises de QUALQUER usuário — um consentimento
 *       distinto de só guardar o próprio histórico.</li>
 * </ul>
 */
public record Consentimento(
        UUID id, UUID usuarioId, boolean manterHistorico, boolean contribuirParaRag, Instant concedidoEm) {

    public Consentimento {
        Objects.requireNonNull(id, "id não pode ser nulo");
        Objects.requireNonNull(usuarioId, "usuarioId não pode ser nulo");
        Objects.requireNonNull(concedidoEm, "concedidoEm não pode ser nulo");
    }
}
