package com.divergia.application.port.out;

import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.TipoDesvio;

import java.util.List;

/**
 * O que o domínio precisa de um modelo de linguagem: avaliar um par de
 * textos (original/editado) e apontar onde a versão editada diverge do
 * original em sentido, posição ou intensidade; e sugerir uma reescrita
 * fiel ao sentido original para um trecho já identificado.
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

    /**
     * @param trechoOriginal trecho do texto antes da edição
     * @param trechoEditado trecho do texto depois da edição, onde a deriva foi identificada
     * @param tipoDesvio dimensão da deriva já identificada (sentido/posição/intensidade)
     * @param explicacao explicação já dada para a deriva
     * @param exemplosRelevantes exemplos recuperados via RAG para calibrar a sugestão
     * @return 3 reescritas alternativas do trecho editado, cada uma fiel ao sentido do
     *         trecho original mas com fraseio distinto entre si
     */
    List<String> sugerirReescrita(
            String trechoOriginal,
            String trechoEditado,
            TipoDesvio tipoDesvio,
            String explicacao,
            List<ExemploRag> exemplosRelevantes);
}
