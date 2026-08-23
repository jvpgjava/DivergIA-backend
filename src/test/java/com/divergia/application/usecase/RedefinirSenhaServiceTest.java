package com.divergia.application.usecase;

import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.TokenRecuperacaoSenhaRepositoryPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.TokenRecuperacaoSenha;
import com.divergia.domain.model.Usuario;
import com.divergia.domain.service.GeradorDeTokenSeguro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedefinirSenhaServiceTest {

    @Mock
    private TokenRecuperacaoSenhaRepositoryPort tokenRepository;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    private RedefinirSenhaService service;
    private final GeradorDeTokenSeguro gerador = new GeradorDeTokenSeguro();

    @BeforeEach
    void setUp() {
        service = new RedefinirSenhaService(tokenRepository, usuarioRepository, passwordEncoder);
    }

    @Test
    void deveAtualizarSenhaEMarcarTokenComoUsadoQuandoTokenValido() {
        String tokenBruto = gerador.gerar();
        String hash = gerador.hash(tokenBruto);
        UUID usuarioId = UUID.randomUUID();
        Instant agora = Instant.now();
        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha(
                UUID.randomUUID(), usuarioId, hash, agora, agora.plusSeconds(1800), null);
        Usuario usuario = new Usuario(usuarioId, "Ana", "ana@example.com", "hash-antigo", agora);

        given(tokenRepository.buscarPorHash(hash)).willReturn(Optional.of(token));
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.of(usuario));
        given(passwordEncoder.codificar("novaSenha123")).willReturn("hash-novo");

        service.redefinir(tokenBruto, "novaSenha123");

        verify(usuarioRepository).salvar(any(Usuario.class));
        verify(tokenRepository).marcarComoUsado(token.id());
    }

    @Test
    void deveLancarExcecaoQuandoTokenNaoEncontrado() {
        String tokenBruto = gerador.gerar();
        given(tokenRepository.buscarPorHash(gerador.hash(tokenBruto))).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.redefinir(tokenBruto, "novaSenha123"))
                .isInstanceOf(TokenInvalidoOuExpiradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoTokenJaFoiUsado() {
        String tokenBruto = gerador.gerar();
        String hash = gerador.hash(tokenBruto);
        Instant agora = Instant.now();
        TokenRecuperacaoSenha tokenJaUsado = new TokenRecuperacaoSenha(
                UUID.randomUUID(), UUID.randomUUID(), hash, agora, agora.plusSeconds(1800), agora);

        given(tokenRepository.buscarPorHash(hash)).willReturn(Optional.of(tokenJaUsado));

        assertThatThrownBy(() -> service.redefinir(tokenBruto, "novaSenha123"))
                .isInstanceOf(TokenInvalidoOuExpiradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoTokenExpirado() {
        String tokenBruto = gerador.gerar();
        String hash = gerador.hash(tokenBruto);
        Instant passado = Instant.now().minusSeconds(3600);
        TokenRecuperacaoSenha tokenExpirado = new TokenRecuperacaoSenha(
                UUID.randomUUID(), UUID.randomUUID(), hash, passado, passado.plusSeconds(1800), null);

        given(tokenRepository.buscarPorHash(hash)).willReturn(Optional.of(tokenExpirado));

        assertThatThrownBy(() -> service.redefinir(tokenBruto, "novaSenha123"))
                .isInstanceOf(TokenInvalidoOuExpiradoException.class);
    }
}
