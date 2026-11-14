package com.divergia.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

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

    public boolean pertenceA(UUID outroUsuarioId) {
        return usuarioId.equals(outroUsuarioId);
    }
}
