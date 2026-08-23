package com.divergia.application.port.out;

import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.OrigemExemplo;
import com.divergia.domain.model.TipoDesvio;

import java.util.List;

/**
 * O que o domínio precisa da base vetorial de exemplos (RAG): buscar
 * exemplos semelhantes a um texto, e guardar novos exemplos nela.
 */
public interface VectorStorePort {

    /**
     * Busca os exemplos mais semanticamente semelhantes ao texto informado.
     *
     * @param texto texto de referência para a busca
     * @param quantidade número máximo de exemplos a retornar
     */
    List<ExemploRag> buscarSimilares(String texto, int quantidade);

    /**
     * Calcula o embedding do par de textos e persiste como um novo exemplo.
     */
    ExemploRag salvar(String textoOriginal, String textoEditado, TipoDesvio tipoDesvio, OrigemExemplo origem);
}
