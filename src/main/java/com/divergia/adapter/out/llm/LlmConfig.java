package com.divergia.adapter.out.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmConfig.class);

    @Bean
    public ChatModel chatModel(LlmProperties properties) {
        if (!properties.temChave()) {
            log.warn("ABACUS_API_KEY não configurada: análise e reescrita vão responder erro até a chave "
                    + "ser definida no .env (veja .env.example).");
        }
        return OpenAiChatModel.builder()
                .baseUrl(properties.baseUrl())
                // O cliente exige um valor; sem chave real o adapter recusa antes de chamar o provedor.
                .apiKey(properties.temChave() ? properties.apiKey() : "sem-chave")
                .modelName(properties.modelName())
                .timeout(Duration.ofSeconds(properties.timeoutSegundos()))
                .maxRetries(properties.maxTentativas())
                .build();
    }
}
