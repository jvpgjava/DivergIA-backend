package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Consentimento(
        UUID id, UUID usuarioId, boolean manterHistorico, boolean contribuirParaRag, Instant concedidoEm) {

    public Consentimento {
        Objects.requireNonNull(id, "id não pode ser nulo");
        Objects.requireNonNull(usuarioId, "usuarioId não pode ser nulo");
        Objects.requireNonNull(concedidoEm, "concedidoEm não pode ser nulo");
    }
}
