package com.divergia.adapter.in.web;

import com.divergia.application.port.out.EmailPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo de autenticação (Fase 3) de ponta a ponta contra o Postgres
 * real. O envio de e-mail é mockado (ver {@link EmailPort}) para não
 * depender de SMTP real durante o teste/CI.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EmailPort emailPort;

    private String emailUnico(String prefixo) {
        return prefixo + "+" + UUID.randomUUID() + "@example.com";
    }

    @Test
    void deveCadastrarLogarEExcluirAContaDePontaAPonta() throws Exception {
        String email = emailUnico("fluxo-completo");

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CadastroBody("Ana Teste", email, "senha12345"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(content().string(not(containsString("senha"))));

        String token = login(email, "senha12345");

        mockMvc.perform(delete("/api/auth/conta").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // depois de excluída, tentar logar de novo deve falhar (usuário não existe mais)
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody(email, "senha12345"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void naoDeveCadastrarComEmailJaExistente() throws Exception {
        String email = emailUnico("duplicado");
        CadastroBody corpo = new CadastroBody("Ana", email, "senha12345");

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRejeitarLoginComSenhaIncorreta() throws Exception {
        String email = emailUnico("senha-errada");
        cadastrar("Ana", email, "senha12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody(email, "senha-errada"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meDeveDevolverNomeEEmailDoUsuarioDoToken() throws Exception {
        String email = emailUnico("meu-perfil");
        cadastrar("Ana Clara", email, "senha12345");
        String token = login(email, "senha12345");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Clara"))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void meDeveRejeitarAcessoSemToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    if (sc != 401 && sc != 403) {
                        throw new AssertionError("Esperado 401 ou 403, recebido " + sc);
                    }
                });
    }

    @Test
    void deveRejeitarAcessoAContaSemToken() throws Exception {
        mockMvc.perform(delete("/api/auth/conta"))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    if (sc != 401 && sc != 403) {
                        throw new AssertionError("Esperado 401 ou 403, recebido " + sc);
                    }
                });
    }

    @Test
    void tokenDeveSerRejeitadoAposLogout() throws Exception {
        String email = emailUnico("logout");
        cadastrar("Ana", email, "senha12345");
        String token = login(email, "senha12345");

        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/auth/conta").header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int sc = result.getResponse().getStatus();
                    if (sc != 401 && sc != 403) {
                        throw new AssertionError("Esperado 401 ou 403 após logout, recebido " + sc);
                    }
                });
    }

    @Test
    void deveRedefinirSenhaComTokenValidoRecebidoPorEmail() throws Exception {
        String email = emailUnico("redefinir");
        cadastrar("Ana", email, "senha-antiga-123");

        mockMvc.perform(post("/api/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecuperarSenhaBody(email))))
                .andExpect(status().isAccepted());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        then(emailPort).should().enviarRecuperacaoSenha(eq(email), tokenCaptor.capture());
        String tokenBruto = tokenCaptor.getValue();
        assertThat(tokenBruto).isNotBlank();

        mockMvc.perform(post("/api/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RedefinirSenhaBody(tokenBruto, "senha-nova-456"))))
                .andExpect(status().isOk());

        // senha antiga não funciona mais, nova funciona
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody(email, "senha-antiga-123"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody(email, "senha-nova-456"))))
                .andExpect(status().isOk());
    }

    @Test
    void naoDeveRevelarSeEmailExisteAoSolicitarRecuperacaoDeSenha() throws Exception {
        mockMvc.perform(post("/api/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RecuperarSenhaBody(emailUnico("nao-existe")))))
                .andExpect(status().isAccepted());

        then(emailPort).shouldHaveNoInteractions();
    }

    @Test
    void deveRejeitarRedefinicaoComTokenInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RedefinirSenhaBody("token-que-nao-existe", "senha-nova-456"))))
                .andExpect(status().isBadRequest());
    }

    private void cadastrar(String nome, String email, String senha) throws Exception {
        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CadastroBody(nome, email, senha))))
                .andExpect(status().isCreated());
    }

    private String login(String email, String senha) throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody(email, senha))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(corpo).get("accessToken").asText();
    }

    private record CadastroBody(String nome, String email, String senha) {
    }

    private record LoginBody(String email, String senha) {
    }

    private record RecuperarSenhaBody(String email) {
    }

    private record RedefinirSenhaBody(String token, String novaSenha) {
    }
}
