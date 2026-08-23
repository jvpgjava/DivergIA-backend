package com.divergia.adapter.out.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "divergia.llm")
public record LlmProperties(String baseUrl, String apiKey, String modelName) {
}
