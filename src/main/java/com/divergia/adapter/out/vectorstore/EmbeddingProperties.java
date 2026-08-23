package com.divergia.adapter.out.vectorstore;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "divergia.embedding")
public record EmbeddingProperties(String baseUrl, String apiKey, String modelName, int outputDimensionality) {
}
