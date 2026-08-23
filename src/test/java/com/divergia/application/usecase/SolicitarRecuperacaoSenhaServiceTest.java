package com.divergia.application.usecase;

import com.divergia.application.port.out.EmailPort;
import com.divergia.application.port.out.TokenRecuperacaoSenhaRepositoryPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.TokenRecuperacaoSenha;
import com.divergia.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SolicitarRecuperacaoSenhaServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private TokenRecuperacaoSenhaRepositoryPort tokenRepository;

    @Mock
    private EmailPort emailPort;

    private SolicitarRecuperacaoSenhaService service;

    @BeforeEach
    void setUp() {
        service = new SolicitarRecuperacaoSenhaService(usuarioRepository, tokenRepository, emailPort, 30);
    }

    @Test
    void deveGerarTokenPersistirEEnviarEmailQuandoUsuarioExiste() {
        Usuario usuario = new Usuario(UUID.randomUUID(), "Ana", "ana@example.com", "hash", Instant.now());
        given(usuarioRepository.buscarPorEmail("ana@example.com")).willReturn(Optional.of(usuario));
        given(tokenRepository.salvar(any(TokenRecuperacaoSenha.class))).willAnswer(inv -> inv.getArgument(0));

        service.solicitar("ana@example.com");

        ArgumentCaptor<TokenRecuperacaoSenha> tokenCaptor = ArgumentCaptor.forClass(TokenRecuperacaoSenha.class);
        verify(tokenRepository).salvar(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().usuarioId()).isEqualTo(usuario.id());
        assertThat(tokenCaptor.getValue().usadoEm()).isNull();

        verify(emailPort).enviarRecuperacaoSenha(eq("ana@example.com"), anyString());
    }

    @Test
    void naoDeveFazerNadaQuandoUsuarioNaoExisteParaNaoRevelarEmailCadastrado() {
        given(usuarioRepository.buscarPorEmail("desconhecido@example.com")).willReturn(Optional.empty());

        service.solicitar("desconhecido@example.com");

        verify(tokenRepository, never()).salvar(any());
        verify(emailPort, never()).enviarRecuperacaoSenha(anyString(), anyString());
    }
}
