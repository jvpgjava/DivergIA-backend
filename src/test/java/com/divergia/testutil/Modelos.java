package com.divergia.testutil;

import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.AvaliacaoDoModelo;
import com.divergia.domain.model.TriagemDaEntrada;

import java.util.List;

public final class Modelos {

    private Modelos() {
    }

    public static AvaliacaoDoModelo modelo(AvaliacaoDeDeriva... derivas) {
        return new AvaliacaoDoModelo(TriagemDaEntrada.valida(), List.of(derivas));
    }
}
