package com.divergia.application.port.out;

import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.OrigemExemplo;
import com.divergia.domain.model.TipoDesvio;

import java.util.List;

public interface VectorStorePort {
    List<ExemploRag> buscarSimilares(String texto, int quantidade);

    ExemploRag salvar(String textoOriginal, String textoEditado, TipoDesvio tipoDesvio, OrigemExemplo origem);
}
