package com.divergia.adapter.out.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Aponta o módulo OpenAI-compatible do LangChain4j para a API da Abacus.AI
 * (RouteLLM), configurada com o identificador do modelo Claude ativo na
 * conta — que não é necessariamente igual ao nome usado na API oficial da
 * Anthropic.
 */
@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfig {

    @Bean
    public ChatModel chatModel(LlmProperties properties) {
        return OpenAiChatModel.builder()
                .baseUrl(properties.baseUrl())
                .apiKey(properties.apiKey())
                .modelName(properties.modelName())
                .build();
    }
}
