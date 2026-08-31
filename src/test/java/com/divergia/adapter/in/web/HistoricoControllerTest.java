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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

/**
 * Fluxo completo de histórico, tendência e exclusão (Fase 7) de ponta a
 * ponta contra o Postgres real. LLM e base vetorial são mockados — nenhuma
 * chamada de rede real acontece no teste/CI.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HistoricoControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    private String analisar(String token, String original, String editado, TipoDesvio tipo, double intensidade)
            throws Exception {
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(org.mockito.ArgumentMatchers.eq(original), org.mockito.ArgumentMatchers.eq(editado), any()))
                .willReturn(List.of(new AvaliacaoDeDeriva(tipo, original, editado, "explicacao de teste", intensidade)));

        String corpo = mockMvc.perform(multipart("/api/analises")
                        .param("textoOriginal", original)
                        .param("textoEditado", editado)
                        .param("manterHistorico", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(corpo).get("analiseId").asText();
    }

    @Test
    void fluxoCompletoDeHistoricoTendenciaEExclusao() throws Exception {
        String token = cadastrarELogar("historico-completo+" + UUID.randomUUID() + "@example.com");

        String analiseId1 = analisar(token, "o prazo é de dois anos", "o prazo é rápido", TipoDesvio.SENTIDO, 0.8);
        String analiseId2 = analisar(
                token, "o produto é bom", "o produto é excepcional", TipoDesvio.INTENSIDADE, 0.6);

        // 1) listar histórico: duas análises
        String corpoLista = mockMvc.perform(get("/api/historico").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode lista = objectMapper.readTree(corpoLista);
        assertThat(lista).hasSize(2);
        // mais recente primeiro (analiseId2, intensidade 0.6 -> 60 pts, INTENSIDADE)
        assertThat(lista.get(0).get("id").asText()).isEqualTo(analiseId2);
        assertThat(lista.get(0).get("pontuacaoIntensidade").asInt()).isEqualTo(60);
        assertThat(lista.get(0).get("tipoDesvioPrincipal").asText()).isEqualTo("INTENSIDADE");
        assertThat(lista.get(0).get("textoPreview").asText()).isEqualTo("o produto é bom");
        assertThat(lista.get(1).get("pontuacaoIntensidade").asInt()).isEqualTo(80);
        assertThat(lista.get(1).get("tipoDesvioPrincipal").asText()).isEqualTo("SENTIDO");

        // 2) detalhe de uma análise específica
        mockMvc.perform(get("/api/historico/" + analiseId1).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analiseId").value(analiseId1))
                .andExpect(jsonPath("$.trechos", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.trechos[0].tipoDesvio").value("SENTIDO"));

        // 3) painel de tendência reflete as duas análises
        mockMvc.perform(get("/api/historico/tendencia").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnalises").value(2))
                .andExpect(jsonPath("$.totalDerivas").value(2))
                .andExpect(jsonPath("$.derivasPorTipo.SENTIDO").value(1))
                .andExpect(jsonPath("$.derivasPorTipo.INTENSIDADE").value(1));

        // 4) excluir uma análise específica
        mockMvc.perform(delete("/api/historico/" + analiseId1).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        String corpoListaAposExcluirUma = mockMvc.perform(
                        get("/api/historico").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(objectMapper.readTree(corpoListaAposExcluirUma)).hasSize(1);

        mockMvc.perform(get("/api/historico/" + analiseId1).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        // 5) excluir todo o histórico restante
        mockMvc.perform(delete("/api/historico").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        String corpoListaVazia = mockMvc.perform(get("/api/historico").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(objectMapper.readTree(corpoListaVazia)).isEmpty();

        mockMvc.perform(get("/api/historico/" + analiseId2).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarAcessoAoHistoricoDeOutroUsuario() throws Exception {
        String tokenDono = cadastrarELogar("dono-historico+" + UUID.randomUUID() + "@example.com");
        String tokenIntruso = cadastrarELogar("intruso-historico+" + UUID.randomUUID() + "@example.com");

        String analiseId = analisar(tokenDono, "original", "editado", TipoDesvio.POSICAO, 0.5);

        mockMvc.perform(get("/api/historico/" + analiseId).header("Authorization", "Bearer " + tokenIntruso))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/historico/" + analiseId).header("Authorization", "Bearer " + tokenIntruso))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRejeitarAcessoAoHistoricoSemToken() throws Exception {
        mockMvc.perform(get("/api/historico"))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    if (sc != 401 && sc != 403) {
                        throw new AssertionError("Esperado 401 ou 403, recebido " + sc);
                    }
                });
    }

    @Test
    void deveDevolverListaEPainelVaziosParaUsuarioSemAnalises() throws Exception {
        String token = cadastrarELogar("sem-historico+" + UUID.randomUUID() + "@example.com");

        String corpoLista = mockMvc.perform(get("/api/historico").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(objectMapper.readTree(corpoLista)).isEmpty();

        mockMvc.perform(get("/api/historico/tendencia").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnalises").value(0))
                .andExpect(jsonPath("$.totalDerivas").value(0));
    }

    private record CadastroBody(String nome, String email, String senha) {
    }

    private record LoginBody(String email, String senha) {
    }
}
