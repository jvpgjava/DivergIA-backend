package com.divergia.application.port.out;

import com.divergia.domain.model.AvaliacaoDoModelo;
import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.TipoDesvio;

import java.util.List;

public interface LlmPort {
    AvaliacaoDoModelo avaliarDerivas(String textoOriginal, String textoEditado, List<ExemploRag> exemplosRelevantes);

    List<String> sugerirReescrita(
            String trechoOriginal,
            String trechoEditado,
            TipoDesvio tipoDesvio,
            String explicacao,
            List<ExemploRag> exemplosRelevantes);

    // Nunca lança: lista vazia significa juiz indisponível, e quem chama segue sem ele.
    List<Boolean> verificarFidelidade(String trechoOriginal, List<String> sugestoes);
}
