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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlterarEmailServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    private AlterarEmailService service;

    @BeforeEach
    void setUp() {
        service = new AlterarEmailService(usuarioRepository, passwordEncoder);
    }

    @Test
    void deveAlterarOEmailQuandoASenhaEstiverCorretaEONovoEmailEstiverLivre() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario(usuarioId, "Ana", "antigo@example.com", "hash", Instant.now(), null);
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.of(usuario));
        given(passwordEncoder.confere("senha-atual", "hash")).willReturn(true);
        given(usuarioRepository.existeComEmail("novo@example.com")).willReturn(false);
        given(usuarioRepository.salvar(any(Usuario.class))).willAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = service.alterar(usuarioId, "novo@example.com", "senha-atual");

        assertThat(resultado.email()).isEqualTo("novo@example.com");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).salvar(captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("novo@example.com");
    }

    @Test
    void deveLancarExcecaoQuandoASenhaEstiverIncorreta() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario(usuarioId, "Ana", "antigo@example.com", "hash", Instant.now(), null);
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.of(usuario));
        given(passwordEncoder.confere("senha-errada", "hash")).willReturn(false);

        assertThatThrownBy(() -> service.alterar(usuarioId, "novo@example.com", "senha-errada"))
                .isInstanceOf(CredenciaisInvalidasException.class);

        verify(usuarioRepository, never()).salvar(any());
    }

    @Test
    void deveLancarExcecaoQuandoONovoEmailJaEstiverEmUsoPorOutraConta() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario(usuarioId, "Ana", "antigo@example.com", "hash", Instant.now(), null);
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.of(usuario));
        given(passwordEncoder.confere("senha-atual", "hash")).willReturn(true);
        given(usuarioRepository.existeComEmail("ocupado@example.com")).willReturn(true);

        assertThatThrownBy(() -> service.alterar(usuarioId, "ocupado@example.com", "senha-atual"))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(usuarioRepository, never()).salvar(any());
    }

    @Test
    void deveDeixarAlterarParaOMesmoEmailQueJaEDoProprioUsuario() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario(usuarioId, "Ana", "ana@example.com", "hash", Instant.now(), null);
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.of(usuario));
        given(passwordEncoder.confere("senha-atual", "hash")).willReturn(true);
        given(usuarioRepository.salvar(any(Usuario.class))).willAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = service.alterar(usuarioId, "ana@example.com", "senha-atual");

        assertThat(resultado.email()).isEqualTo("ana@example.com");
    }
}
