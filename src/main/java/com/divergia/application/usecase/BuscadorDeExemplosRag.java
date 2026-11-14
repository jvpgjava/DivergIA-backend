package com.divergia.application.usecase;

import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.ExemploRag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BuscadorDeExemplosRag {

    private static final Logger log = LoggerFactory.getLogger(BuscadorDeExemplosRag.class);

    private final VectorStorePort vectorStorePort;
    private final int quantidade;

    public BuscadorDeExemplosRag(
            VectorStorePort vectorStorePort,
            @Value("${divergia.analise.quantidade-exemplos-rag:5}") int quantidade) {
        this.vectorStorePort = vectorStorePort;
        this.quantidade = quantidade;
    }

    // Os exemplos só calibram o modelo: falha na busca não pode derrubar a análise.
    public List<ExemploRag> buscar(String textoOriginal, String textoEditado) {
        try {
            return vectorStorePort.buscarSimilares(textoOriginal + "\n" + textoEditado, quantidade);
        } catch (Exception e) {
            log.warn("Falha ao buscar exemplos de RAG, prosseguindo sem eles: {}", e.getMessage());
            return List.of();
        }
    }
}
