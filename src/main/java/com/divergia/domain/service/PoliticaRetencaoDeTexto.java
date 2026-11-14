package com.divergia.domain.service;

import com.divergia.domain.model.Analise;

public class PoliticaRetencaoDeTexto {
    public Analise aplicar(Analise analise) {
        if (analise.manterHistorico()) {
            return analise;
        }
        return new Analise(
                analise.id(),
                analise.usuarioId(),
                null,
                null,
                false,
                analise.criadoEm());
    }
}
