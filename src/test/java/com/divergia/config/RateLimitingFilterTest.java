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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
        registry.add("divergia.rate-limit.analise.capacidade", () -> "2");
        registry.add("divergia.rate-limit.analise.janela-minutos", () -> "1");
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

    @Test
    void deveBloquearAposExcederACapacidadeDeTentativasDeAnalise() throws Exception {
        // Sem token válido mesmo: o rate limiting roda antes da checagem de
        // autenticação, então basta bater na rota repetidas vezes.
        mockMvc.perform(multipart("/api/analises").param("textoOriginal", "a").param("textoEditado", "b"))
                .andExpect(result -> assertNaoEh429(result.getResponse().getStatus()));
        mockMvc.perform(multipart("/api/analises").param("textoOriginal", "a").param("textoEditado", "b"))
                .andExpect(result -> assertNaoEh429(result.getResponse().getStatus()));
        mockMvc.perform(multipart("/api/analises").param("textoOriginal", "a").param("textoEditado", "b"))
                .andExpect(status().isTooManyRequests());
    }

    private void assertNaoEh429(int status) {
        if (status == 429) {
            throw new AssertionError("Não esperava 429 ainda, capacidade não deveria ter sido excedida");
        }
    }

    private record LoginBody(String email, String senha) {
    }
}
