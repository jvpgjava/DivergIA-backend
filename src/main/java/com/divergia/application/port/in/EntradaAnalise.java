package com.divergia.application.port.in;

import java.util.Objects;
import java.util.UUID;

public record EntradaAnalise(UUID usuarioId, EntradaTexto original, EntradaTexto editado, boolean manterHistorico) {

    public EntradaAnalise {
        Objects.requireNonNull(usuarioId, "usuarioId não pode ser nulo");
        Objects.requireNonNull(original, "original não pode ser nulo");
        Objects.requireNonNull(editado, "editado não pode ser nulo");
    }
}
