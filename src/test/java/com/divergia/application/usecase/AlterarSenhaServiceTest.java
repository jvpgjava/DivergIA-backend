package com.divergia.application.usecase;

import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlterarSenhaServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    private AlterarSenhaService service;

    @BeforeEach
    void setUp() {
        service = new AlterarSenhaService(usuarioRepository, passwordEncoder);
    }

    @Test
    void deveAlterarASenhaQuandoASenhaAtualEstiverCorreta() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario(usuarioId, "Ana", "ana@example.com", "hash-antigo", Instant.now(), null);
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.of(usuario));
        given(passwordEncoder.confere("senha-atual", "hash-antigo")).willReturn(true);
        given(passwordEncoder.codificar("senha-nova12")).willReturn("hash-novo");

        service.alterar(usuarioId, "senha-atual", "senha-nova12");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).salvar(captor.capture());
        assertThat(captor.getValue().senhaHash()).isEqualTo("hash-novo");
        assertThat(captor.getValue().email()).isEqualTo("ana@example.com");
    }

    @Test
    void deveLancarExcecaoQuandoASenhaAtualEstiverIncorreta() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario(usuarioId, "Ana", "ana@example.com", "hash-antigo", Instant.now(), null);
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.of(usuario));
        given(passwordEncoder.confere("senha-errada", "hash-antigo")).willReturn(false);

        assertThatThrownBy(() -> service.alterar(usuarioId, "senha-errada", "senha-nova12"))
                .isInstanceOf(CredenciaisInvalidasException.class);

        verify(usuarioRepository, never()).salvar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deveLancarExcecaoQuandoOUsuarioNaoExiste() {
        UUID usuarioId = UUID.randomUUID();
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.alterar(usuarioId, "senha-atual", "senha-nova12"))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }
}
