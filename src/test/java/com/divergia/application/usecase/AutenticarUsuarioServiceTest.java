package com.divergia.application.usecase;

import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.TokenPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.TokenAcesso;
import com.divergia.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AutenticarUsuarioServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenPort tokenPort;

    private AutenticarUsuarioService service;

    @BeforeEach
    void setUp() {
        service = new AutenticarUsuarioService(usuarioRepository, passwordEncoder, tokenPort);
    }

    @Test
    void deveAutenticarERetornarTokenQuandoCredenciaisCorretas() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario(usuarioId, "Ana", "ana@example.com", "hash", Instant.now());
        TokenAcesso tokenEsperado = new TokenAcesso("jwt-valor", "jti-1", usuarioId, Instant.now().plusSeconds(900));

        given(usuarioRepository.buscarPorEmail("ana@example.com")).willReturn(Optional.of(usuario));
        given(passwordEncoder.confere("senha12345", "hash")).willReturn(true);
        given(tokenPort.gerar(usuarioId)).willReturn(tokenEsperado);

        TokenAcesso resultado = service.autenticar("ana@example.com", "senha12345");

        assertThat(resultado).isEqualTo(tokenEsperado);
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoEncontrado() {
        given(usuarioRepository.buscarPorEmail("desconhecido@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.autenticar("desconhecido@example.com", "senha12345"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        Usuario usuario = new Usuario(UUID.randomUUID(), "Ana", "ana@example.com", "hash", Instant.now());
        given(usuarioRepository.buscarPorEmail("ana@example.com")).willReturn(Optional.of(usuario));
        given(passwordEncoder.confere("senha-errada", "hash")).willReturn(false);

        assertThatThrownBy(() -> service.autenticar("ana@example.com", "senha-errada"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}
