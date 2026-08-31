package com.divergia.adapter.in.web;

import com.divergia.application.port.out.EmailPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirma que a API responde erros com um único shape padronizado
 * ({@code timestamp/status/error/message/path}) — Fase 8.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailPort emailPort;

    @Test
    void deveDevolverShapePadronizadoParaEmailJaCadastrado() throws Exception {
        String email = "erro-padrao+" + UUID.randomUUID() + "@example.com";
        String corpo = "{\"nome\":\"Teste\",\"email\":\"" + email + "\",\"senha\":\"senha12345\"}";

        mockMvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/auth/cadastro"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deveDevolverShapePadronizadoParaErroDeValidacaoBeanValidation() throws Exception {
        // email em branco e senha curta demais — dispara MethodArgumentNotValidException
        String corpoInvalido = "{\"nome\":\"Teste\",\"email\":\"\",\"senha\":\"123\"}";

        mockMvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(corpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/auth/cadastro"));
    }

    @Test
    void deveDevolverShapePadronizadoParaRecursoNaoEncontrado() throws Exception {
        String email = "erro-404+" + UUID.randomUUID() + "@example.com";
        String corpo = "{\"nome\":\"Teste\",\"email\":\"" + email + "\",\"senha\":\"senha12345\"}";
        mockMvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());

        String login = "{\"email\":\"" + email + "\",\"senha\":\"senha12345\"}";
        String corpoLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(login))
                .andReturn().getResponse().getContentAsString();
        String token = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(corpoLogin).get("accessToken").asText();

        mockMvc.perform(get("/api/historico/" + UUID.randomUUID()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").exists());
    }
}
