package com.divergia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Isola a validação do rate limiting de login/cadastro com uma capacidade
 * bem pequena — o profile {@code test} normalmente usa uma capacidade alta
 * (ver application.yml) para não interferir nos demais testes de fluxo.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RateLimitingFilterTest {

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) {
        registry.add("divergia.rate-limit.auth.capacidade", () -> "2");
        registry.add("divergia.rate-limit.auth.janela-minutos", () -> "1");
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deveBloquearAposExcederACapacidadeDeTentativasDeLogin() throws Exception {
        String corpo = objectMapper.writeValueAsString(
                new LoginBody("rate-limit-teste@example.com", "senha-qualquer"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isTooManyRequests());
    }

    private record LoginBody(String email, String senha) {
    }
}
