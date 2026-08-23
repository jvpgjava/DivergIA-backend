package com.divergia.adapter.out.vectorstore;

import com.divergia.adapter.out.persistence.ExemploRagJpaEntity;
import com.divergia.adapter.out.persistence.ExemploRagJpaRepository;
import com.divergia.adapter.out.persistence.ExemploRagMapper;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.OrigemExemplo;
import com.divergia.domain.model.TipoDesvio;
import com.pgvector.PGvector;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GeminiVectorStoreAdapter implements VectorStorePort {

    private final EmbeddingModel embeddingModel;
    private final ExemploRagJpaRepository repository;

    public GeminiVectorStoreAdapter(EmbeddingModel embeddingModel, ExemploRagJpaRepository repository) {
        this.embeddingModel = embeddingModel;
        this.repository = repository;
    }

    @Override
    public List<ExemploRag> buscarSimilares(String texto, int quantidade) {
        float[] embedding = embed(texto);
        String embeddingLiteral = new PGvector(embedding).getValue();
        return repository.buscarMaisSimilares(embeddingLiteral, quantidade).stream()
                .map(ExemploRagMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ExemploRag salvar(String textoOriginal, String textoEditado, TipoDesvio tipoDesvio, OrigemExemplo origem) {
        float[] embedding = embed(textoOriginal + "\n" + textoEditado);
        ExemploRag exemplo = new ExemploRag(
                UUID.randomUUID(), textoOriginal, textoEditado, tipoDesvio, embedding, origem, Instant.now());
        ExemploRagJpaEntity salvo = repository.saveAndFlush(ExemploRagMapper.toEntity(exemplo));
        return ExemploRagMapper.toDomain(salvo);
    }

    private float[] embed(String texto) {
        return embeddingModel.embed(texto).content().vector();
    }
}
