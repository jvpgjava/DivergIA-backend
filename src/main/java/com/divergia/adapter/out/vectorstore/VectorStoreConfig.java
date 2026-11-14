package com.divergia.adapter.out.vectorstore;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmbeddingProperties.class)
public class VectorStoreConfig {

    @Bean
    public EmbeddingModel embeddingModel(EmbeddingProperties properties) {
        // Embeddings vêm do Gemini porque a Abacus.AI/RouteLLM não oferece esse serviço.
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
