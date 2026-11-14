package com.divergia.adapter.in.web;

import com.divergia.adapter.out.persistence.UsuarioJpaRepository;
import com.divergia.application.port.out.EmailPort;
import com.divergia.application.port.out.ExtracaoDocumentoPort;
import com.divergia.application.port.out.LlmPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.AvaliacaoDeDeriva;
import static com.divergia.testutil.Modelos.modelo;
import com.divergia.domain.model.TipoDesvio;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExcluirHistoricoSemTransacaoAmbienteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @MockitoBean
    private LlmPort llmPort;

    @MockitoBean
    private VectorStorePort vectorStorePort;

    @MockitoBean
    private ExtracaoDocumentoPort extracaoDocumentoPort;

    @MockitoBean
    private EmailPort emailPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID usuarioIdCriado;

    @AfterEach
    void limparUsuarioCriado() {
        if (usuarioIdCriado != null) {
            usuarioRepository.deleteById(usuarioIdCriado);
        }
    }

    @Test
    void excluirTodoOHistoricoDeveFuncionarSemTransacaoAmbienteDoTeste() throws Exception {
        String email = "sem-transacao-ambiente+" + UUID.randomUUID() + "@example.com";
        String senha = "senha12345";

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CadastroBody("Usuário Teste", email, senha))))
                .andExpect(status().isCreated());

        String corpoLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody(email, senha))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(corpoLogin).get("accessToken").asText();

        String corpoMe = mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getContentAsString();
        usuarioIdCriado = UUID.fromString(objectMapper.readTree(corpoMe).get("id").asText());

        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(modelo(
                new AvaliacaoDeDeriva(TipoDesvio.SENTIDO, "a reunião foi marcada para segunda de manhã", "a reunião talvez aconteça na segunda", "explicacao de teste", 0.6)));

        mockMvc.perform(multipart("/api/analises")
                        .param("textoOriginal", "a reunião foi marcada para segunda de manhã")
                        .param("textoEditado", "a reunião talvez aconteça na segunda")
                        .param("manterHistorico", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/historico").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        String corpoListaVazia = mockMvc.perform(get("/api/historico").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(objectMapper.readTree(corpoListaVazia)).isEmpty();
    }

    private record CadastroBody(String nome, String email, String senha) {
    }

    private record LoginBody(String email, String senha) {
    }
}
