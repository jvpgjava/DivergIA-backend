package com.divergia.adapter.in.web;

import com.divergia.application.port.out.EmailPort;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConsentimentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailPort emailPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String cadastrarELogar(String email) throws Exception {
        String senha = "senha12345";
        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CadastroBody("Usuário Teste", email, senha))))
                .andExpect(status().isCreated());

        String corpo = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody(email, senha))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(corpo).get("accessToken").asText();
    }

    @Test
    void deveDevolverConsentimentoPadraoPrivacyByDefaultAntesDeQualquerConfiguracao() throws Exception {
        String token = cadastrarELogar("consentimento-padrao+" + UUID.randomUUID() + "@example.com");

        mockMvc.perform(get("/api/consentimento").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manterHistorico").value(false))
                .andExpect(jsonPath("$.contribuirParaRag").value(false));
    }

    @Test
    void deveAtualizarERefletirConsentimentoNaProximaLeitura() throws Exception {
        String token = cadastrarELogar("consentimento-atualiza+" + UUID.randomUUID() + "@example.com");

        mockMvc.perform(put("/api/consentimento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"manterHistorico\":true,\"contribuirParaRag\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manterHistorico").value(true))
                .andExpect(jsonPath("$.contribuirParaRag").value(true));

        mockMvc.perform(get("/api/consentimento").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manterHistorico").value(true))
                .andExpect(jsonPath("$.contribuirParaRag").value(true));

        // atualiza de novo, revogando — a leitura deve refletir o mais recente
        mockMvc.perform(put("/api/consentimento")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"manterHistorico\":true,\"contribuirParaRag\":false}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/consentimento").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manterHistorico").value(true))
                .andExpect(jsonPath("$.contribuirParaRag").value(false));
    }

    @Test
    void deveRejeitarAcessoSemToken() throws Exception {
        mockMvc.perform(get("/api/consentimento"))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    if (sc != 401 && sc != 403) {
                        throw new AssertionError("Esperado 401 ou 403, recebido " + sc);
                    }
                });
    }

    private record CadastroBody(String nome, String email, String senha) {
    }

    private record LoginBody(String email, String senha) {
    }
}
