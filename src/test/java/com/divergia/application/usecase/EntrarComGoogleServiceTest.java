package com.divergia.application.usecase;

import com.divergia.application.port.out.EmailPort;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.TokenPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.application.port.out.VerificadorGooglePort;
import com.divergia.domain.model.IdentidadeGoogle;
import com.divergia.domain.model.TokenAcesso;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EntrarComGoogleServiceTest {

    @Mock
    private VerificadorGooglePort verificador;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenPort tokenPort;

    @Mock
    private EmailPort emailPort;

    private EntrarComGoogleService service;
    private final TokenAcesso token = mock(TokenAcesso.class);

    @BeforeEach
    void setUp() {
        service = new EntrarComGoogleService(verificador, usuarioRepository, passwordEncoder, tokenPort, emailPort);
    }

    @Test
    void contaExistenteComOMesmoEmailEntraSemCriarOutra() {
        Usuario existente = new Usuario(UUID.randomUUID(), "Ana", "ana@gmail.com", "hash", Instant.now(), null);
        given(verificador.configurado()).willReturn(true);
        given(verificador.verificar("id-token")).willReturn(
                Optional.of(new IdentidadeGoogle("1", "Ana@Gmail.com", "Ana Souza")));
        given(usuarioRepository.buscarPorEmail("ana@gmail.com")).willReturn(Optional.of(existente));
        given(tokenPort.gerar(existente.id())).willReturn(token);

        assertThat(service.entrar("id-token")).isSameAs(token);
        verify(usuarioRepository, never()).salvar(any());
    }

    @Test
    void primeiroAcessoCriaContaComNomeDoGoogleESenhaAleatoria() {
        given(verificador.configurado()).willReturn(true);
        given(verificador.verificar("id-token")).willReturn(
                Optional.of(new IdentidadeGoogle("1", "novo@gmail.com", "João 🚀 <Silva>")));
        given(usuarioRepository.buscarPorEmail("novo@gmail.com")).willReturn(Optional.empty());
        given(passwordEncoder.codificar(anyString())).willReturn("hash-aleatorio");
        given(usuarioRepository.salvar(any(Usuario.class))).willAnswer(inv -> inv.getArgument(0));
        given(tokenPort.gerar(any())).willReturn(token);

        assertThat(service.entrar("id-token")).isSameAs(token);

        ArgumentCaptor<Usuario> criado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).salvar(criado.capture());
        assertThat(criado.getValue().email()).isEqualTo("novo@gmail.com");
        assertThat(criado.getValue().nome()).isEqualTo("João Silva");
        assertThat(criado.getValue().senhaHash()).isEqualTo("hash-aleatorio");
        verify(emailPort).enviarBoasVindas("novo@gmail.com", "João Silva");
    }

    @Test
    void tokenInvalidoNaoEntraNemCria() {
        given(verificador.configurado()).willReturn(true);
        given(verificador.verificar("falso")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.entrar("falso")).isInstanceOf(LoginGoogleInvalidoException.class);
        verify(usuarioRepository, never()).salvar(any());
    }

    @Test
    void semConfiguracaoOLoginComGoogleFicaIndisponivel() {
        given(verificador.configurado()).willReturn(false);

        assertThatThrownBy(() -> service.entrar("qualquer")).isInstanceOf(LoginGoogleIndisponivelException.class);
    }

    @Test
    void nomeSemCaracteresValidosUsaOInicioDoEmail() {
        assertThat(EntrarComGoogleService.nomeSeguro("🚀🚀", "maria.lima@gmail.com")).isEqualTo("maria.lima");
        assertThat(EntrarComGoogleService.nomeSeguro(null, "x@gmail.com")).isEqualTo("x");
        assertThat(EntrarComGoogleService.nomeSeguro("A".repeat(300), "a@b.com")).hasSize(100);
    }

    @Test
    void contaCriadaComSenhaEmOutraCaixaEReconhecidaNoGoogleSemDuplicar() {
        Usuario existente = new Usuario(UUID.randomUUID(), "Ana", "ana@gmail.com", "hash", Instant.now(), null);
        given(verificador.configurado()).willReturn(true);
        given(verificador.verificar("id-token")).willReturn(
                Optional.of(new IdentidadeGoogle("1", "ANA@gmail.com", "Ana")));
        given(usuarioRepository.buscarPorEmail("ana@gmail.com")).willReturn(Optional.of(existente));
        given(tokenPort.gerar(existente.id())).willReturn(token);

        service.entrar("id-token");

        verify(usuarioRepository).buscarPorEmail("ana@gmail.com");
        verify(usuarioRepository, never()).salvar(any());
    }
}
