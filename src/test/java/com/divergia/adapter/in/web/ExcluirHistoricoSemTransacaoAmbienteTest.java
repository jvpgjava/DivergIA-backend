package com.divergia.adapter.in.web;

import com.divergia.adapter.out.persistence.UsuarioJpaRepository;
import com.divergia.application.port.out.EmailPort;
import com.divergia.application.port.out.ExtracaoDocumentoPort;
import com.divergia.application.port.out.LlmPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.AvaliacaoDeDeriva;
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

/**
 * Cobre especificamente {@code DELETE /api/historico} SEM a rede de proteção
 * de um {@code @Transactional} na própria classe de teste — de propósito.
 *
 * <p>{@link HistoricoControllerTest} já teria pego esse fluxo, mas é
 * {@code @Transactional} na classe: isso abre uma transação por fora do
 * teste inteiro, e essa transação "emprestada" é suficiente pra satisfazer
 * o {@code deleteByUsuarioId} (uma query derivada, que faz
 * buscar-e-remover(entidade) e por isso exige uma transação real ativa no
 * ponto da chamada) mesmo que o método de serviço não declare
 * {@code @Transactional} nenhum. Foi exatamente esse mascaramento que
 * deixou passar o bug em que excluir o histórico funcionava no teste mas
 * quebrava com 500 ("No EntityManager with actual transaction available")
 * em produção. Esta classe roda sem esse guarda-chuva — a mesma condição de
 * uma requisição HTTP real — pra garantir que a ausência de
 * {@code @Transactional} no serviço nunca mais volte a passar despercebida.
 */
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
        // Sem @Transactional na classe não há rollback automático — a
        // exclusão em cascata (usuario -> analise -> trecho_deriva) limpa
        // tudo que o próprio teste não tiver apagado.
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
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(List.of(
                new AvaliacaoDeDeriva(TipoDesvio.SENTIDO, "original", "editado", "explicacao de teste", 0.6)));

        mockMvc.perform(multipart("/api/analises")
                        .param("textoOriginal", "original")
                        .param("textoEditado", "editado")
                        .param("manterHistorico", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        // O ponto central deste teste: sem transação ambiente cobrindo a
        // chamada, isto tinha que dar 500 antes da correção.
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
