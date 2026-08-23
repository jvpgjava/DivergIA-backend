package com.divergia.application.port.out;

import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.ExemploRag;

import java.util.List;

/**
 * O que o domínio precisa de um modelo de linguagem: avaliar um par de
 * textos (original/editado) e apontar onde a versão editada diverge do
 * original em sentido, posição ou intensidade.
 */
public interface LlmPort {

    /**
     * @param textoOriginal texto antes da edição
     * @param textoEditado texto depois da edição
     * @param exemplosRelevantes exemplos recuperados via RAG para calibrar o julgamento
     * @return derivas identificadas; lista vazia se nenhuma for encontrada
     */
    List<AvaliacaoDeDeriva> avaliarDerivas(
            String textoOriginal, String textoEditado, List<ExemploRag> exemplosRelevantes);
}
