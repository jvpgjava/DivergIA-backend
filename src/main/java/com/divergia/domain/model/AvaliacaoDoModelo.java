package com.divergia.domain.model;

import java.util.List;
import java.util.Objects;

public record AvaliacaoDoModelo(TriagemDaEntrada triagem, List<AvaliacaoDeDeriva> derivas) {

    public AvaliacaoDoModelo {
        Objects.requireNonNull(triagem, "triagem não pode ser nula");
        derivas = derivas == null ? List.of() : List.copyOf(derivas);
    }
}
