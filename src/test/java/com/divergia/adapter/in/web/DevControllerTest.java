package com.divergia.adapter.in.web;

import com.divergia.application.port.out.EmailPort;
import com.divergia.application.port.out.ExtracaoDocumentoPort;
import com.divergia.application.port.out.LlmPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.TipoDesvio;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.divergia.testutil.Modelos.modelo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DevControllerTest {

    private static final String SENHA = "senha12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @MockitoBean
    private LlmPort llmPort;

    @MockitoBean
    private VectorStorePort vectorStorePort;

    @MockitoBean
    private ExtracaoDocumentoPort extracaoDocumentoPort;

    @MockitoBean
    private EmailPort emailPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String cadastrarELogar(String email) throws Exception {
        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("nome", "Usuário Teste", "email", email, "senha", SENHA))))
                .andExpect(status().isCreated());

        String corpo = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("email", email, "senha", SENHA))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(corpo).get("accessToken").asText();
    }

    private void promoverADev(String email) {
        jdbc.update("UPDATE usuario SET papel = 'DEV' WHERE lower(email) = lower(?)", email);
        entityManager.clear();
    }

    private String analisar(String token, String original, String editado, boolean manterHistorico, TipoDesvio tipo)
            throws Exception {
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(org.mockito.ArgumentMatchers.eq(original), org.mockito.ArgumentMatchers.eq(editado), any()))
                .willReturn(modelo(new AvaliacaoDeDeriva(tipo, original, editado, "explicacao de teste", 0.7)));

        String corpo = mockMvc.perform(multipart("/api/analises")
                        .param("textoOriginal", original)
                        .param("textoEditado", editado)
                        .param("manterHistorico", String.valueOf(manterHistorico))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(corpo).get("analiseId").asText();
    }

    private JsonNode estatisticas(String token) throws Exception {
        String corpo = mockMvc.perform(get("/api/dev/estatisticas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(corpo);
    }

    @Test
    void usuarioComumRecebe403EnquantoSemTokenRecebe401() throws Exception {
        String token = cadastrarELogar("comum+" + UUID.randomUUID() + "@example.com");

        mockMvc.perform(get("/api/dev/estatisticas").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/dev/estatisticas")).andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioNovoNasceComoUsuarioEORespostaDoPerfilInformaOPapel() throws Exception {
        String email = "papel+" + UUID.randomUUID() + "@example.com";
        String token = cadastrarELogar(email);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("USUARIO"));

        promoverADev(email);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.papel").value("DEV"));
    }

    @Test
    void devContabilizaCadaNovaAnaliseEMantemOTotalMesmoApagandoOHistorico() throws Exception {
        String email = "dev+" + UUID.randomUUID() + "@example.com";
        String token = cadastrarELogar(email);
        promoverADev(email);

        JsonNode antes = estatisticas(token);

        analisar(token, "o prazo é de dois anos", "o prazo é rápido", true, TipoDesvio.SENTIDO);
        analisar(token, "o produto é bom", "o produto é excepcional", false, TipoDesvio.INTENSIDADE);

        JsonNode depois = estatisticas(token);
        assertThat(depois.get("totalAnalises").asLong() - antes.get("totalAnalises").asLong()).isEqualTo(2);
        assertThat(depois.get("totalDerivas").asLong() - antes.get("totalDerivas").asLong()).isEqualTo(2);
        assertThat(depois.get("derivasPorTipo").get("SENTIDO").asLong()
                - antes.get("derivasPorTipo").get("SENTIDO").asLong()).isEqualTo(1);
        assertThat(depois.get("derivasPorTipo").get("INTENSIDADE").asLong()
                - antes.get("derivasPorTipo").get("INTENSIDADE").asLong()).isEqualTo(1);
        JsonNode serie = depois.get("serieDiaria");
        assertThat(serie).hasSize(30);
        assertThat(serie.get(29).get("analises").asLong()).isGreaterThanOrEqualTo(2);

        mockMvc.perform(delete("/api/historico").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(estatisticas(token).get("totalAnalises").asLong()).isEqualTo(depois.get("totalAnalises").asLong());
    }

    @Test
    void rebaixarOPapelRevogaOAcessoNaMesmaHoraSemTrocarDeToken() throws Exception {
        String email = "rebaixado+" + UUID.randomUUID() + "@example.com";
        String token = cadastrarELogar(email);
        promoverADev(email);
        mockMvc.perform(get("/api/dev/estatisticas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        jdbc.update("UPDATE usuario SET papel = 'USUARIO' WHERE lower(email) = lower(?)", email);
        entityManager.clear();

        mockMvc.perform(get("/api/dev/estatisticas").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void alterarSenhaNaoDesfazAPromocaoFeitaPorFora() throws Exception {
        String email = "senha+" + UUID.randomUUID() + "@example.com";
        String token = cadastrarELogar(email);
        promoverADev(email);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/auth/senha")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("senhaAtual", SENHA, "novaSenha", "outraSenha12345"))))
                .andExpect(status().is2xxSuccessful());

        String papel = jdbc.queryForObject(
                "SELECT papel FROM usuario WHERE lower(email) = lower(?)", String.class, email);
        assertThat(papel).isEqualTo("DEV");
    }

    @Test
    void rejeitaParametroDeDiasInvalido() throws Exception {
        String email = "dias+" + UUID.randomUUID() + "@example.com";
        String token = cadastrarELogar(email);
        promoverADev(email);

        mockMvc.perform(get("/api/dev/estatisticas?dias=abc").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
