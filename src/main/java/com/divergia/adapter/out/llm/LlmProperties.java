package com.divergia.adapter.out.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "divergia.llm")
public record LlmProperties(
        String baseUrl,
        String apiKey,
        String modelName,
        Integer timeoutSegundos,
        Integer maxTentativas,
        Double temperaturaAnalise,
        Double temperaturaReescrita,
        Boolean enviarTemperatura) {

    public LlmProperties {
        timeoutSegundos = timeoutSegundos == null || timeoutSegundos <= 0 ? 90 : timeoutSegundos;
        maxTentativas = maxTentativas == null || maxTentativas < 0 ? 2 : maxTentativas;
        temperaturaAnalise = temperaturaAnalise == null ? 0.0 : temperaturaAnalise;
        temperaturaReescrita = temperaturaReescrita == null ? 0.5 : temperaturaReescrita;
        enviarTemperatura = enviarTemperatura == null || enviarTemperatura;
    }

    public boolean temChave() {
        return apiKey != null && !apiKey.isBlank();
    }
}
