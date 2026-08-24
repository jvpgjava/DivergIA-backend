package com.divergia.adapter.in.web;

import com.divergia.application.port.out.ExtracaoDocumentoPort;
import com.divergia.application.port.out.LlmPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.TipoDesvio;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo de análise (Fase 5) contra o Postgres real. LLM, base
 * vetorial e extração de documento são mockados — nenhuma chamada de rede
 * real acontece no teste/CI.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnaliseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LlmPort llmPort;

    @MockitoBean
    private VectorStorePort vectorStorePort;

    @MockitoBean
    private ExtracaoDocumentoPort extracaoDocumentoPort;

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
    void deveAnalisarParDeTextosColadosComIndicadorDeDerivaCoerente() throws Exception {
        String token = cadastrarELogar("analise-texto+" + UUID.randomUUID() + "@example.com");

        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(eq("o prazo foi adiado em uma semana"), eq("o prazo foi cancelado"), any()))
                .willReturn(List.of(new AvaliacaoDeDeriva(
                        TipoDesvio.SENTIDO, "o prazo foi adiado em uma semana", "o prazo foi cancelado",
                        "a edição mudou completamente o sentido da informação original", 0.9)));

        mockMvc.perform(multipart("/api/analises")
                        .param("textoOriginal", "o prazo foi adiado em uma semana")
                        .param("textoEditado", "o prazo foi cancelado")
                        .param("manterHistorico", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trechos", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.trechos[0].tipoDesvio").value("SENTIDO"))
                .andExpect(jsonPath("$.trechos[0].intensidade").value(0.9));
    }

    @Test
    void deveAnalisarComUploadDeArquivoParaOTextoOriginal() throws Exception {
        String token = cadastrarELogar("analise-arquivo+" + UUID.randomUUID() + "@example.com");

        byte[] conteudoArquivo = "conteudo bruto do pdf".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivoOriginal", "original.pdf", "application/pdf", conteudoArquivo);

        given(extracaoDocumentoPort.extrairTexto(conteudoArquivo, "original.pdf"))
                .willReturn("texto extraído do arquivo pdf");
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(eq("texto extraído do arquivo pdf"), eq("versão editada colada"), any()))
                .willReturn(List.of(new AvaliacaoDeDeriva(
                        TipoDesvio.POSICAO, "texto extraído do arquivo pdf", "versão editada colada",
                        "a posição da informação principal foi alterada", 0.6)));

        mockMvc.perform(multipart("/api/analises")
                        .file(arquivo)
                        .param("textoEditado", "versão editada colada")
                        .param("manterHistorico", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trechos", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.trechos[0].tipoDesvio").value("POSICAO"));

        then(extracaoDocumentoPort).should().extrairTexto(conteudoArquivo, "original.pdf");
    }

    @Test
    void deveRejeitarAnaliseSemToken() throws Exception {
        mockMvc.perform(multipart("/api/analises")
                        .param("textoOriginal", "original")
                        .param("textoEditado", "editado"))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    if (sc != 401 && sc != 403) {
                        throw new AssertionError("Esperado 401 ou 403, recebido " + sc);
                    }
                });
    }

    @Test
    void deveRejeitarQuandoNemTextoNemArquivoForemInformados() throws Exception {
        String token = cadastrarELogar("analise-invalida+" + UUID.randomUUID() + "@example.com");

        mockMvc.perform(multipart("/api/analises")
                        .param("textoEditado", "editado")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    private record CadastroBody(String nome, String email, String senha) {
    }

    private record LoginBody(String email, String senha) {
    }
}
