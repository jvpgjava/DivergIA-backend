package com.divergia.adapter.out.vectorstore;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Modelo de embedding usado para popular e consultar a base vetorial de
 * exemplos (RAG). A Abacus.AI/RouteLLM não oferece embeddings — por isso
 * esta chamada vai direto à API do Gemini (Google AI), fonte separada da
 * usada em {@link com.divergia.adapter.out.llm}.
 */
@Configuration
@EnableConfigurationProperties(EmbeddingProperties.class)
public class VectorStoreConfig {

    @Bean
    public EmbeddingModel embeddingModel(EmbeddingProperties properties) {
        GoogleAiEmbeddingModel.GoogleAiEmbeddingModelBuilder builder = GoogleAiEmbeddingModel.builder()
                .apiKey(properties.apiKey())
                .modelName(properties.modelName())
                .outputDimensionality(properties.outputDimensionality());
        if (properties.baseUrl() != null && !properties.baseUrl().isBlank()) {
            builder.baseUrl(properties.baseUrl());
        }
        return builder.build();
    }
}
