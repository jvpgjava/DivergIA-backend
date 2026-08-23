package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Preferência de retenção de histórico de um usuário: se ele consentiu em
 * manter o texto bruto de suas análises além do necessário para o
 * processamento.
 */
public record Consentimento(UUID id, UUID usuarioId, boolean manterHistorico, Instant concedidoEm) {

    public Consentimento {
        Objects.requireNonNull(id, "id não pode ser nulo");
        Objects.requireNonNull(usuarioId, "usuarioId não pode ser nulo");
        Objects.requireNonNull(concedidoEm, "concedidoEm não pode ser nulo");
    }
}
