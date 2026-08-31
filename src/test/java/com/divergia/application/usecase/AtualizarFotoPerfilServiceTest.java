package com.divergia.application.usecase;

import com.divergia.application.port.out.FotoPerfilPort;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AtualizarFotoPerfilServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private FotoPerfilPort fotoPerfilPort;

    private AtualizarFotoPerfilService service;

    @BeforeEach
    void setUp() {
        service = new AtualizarFotoPerfilService(usuarioRepository, fotoPerfilPort);
    }

    @Test
    void deveSalvarAFotoEAtualizarOUsuarioComAUrl() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario(usuarioId, "Ana", "ana@example.com", "hash", Instant.now(), null);
        byte[] conteudo = {1, 2, 3};
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.of(usuario));
        given(fotoPerfilPort.salvar(eq(usuarioId), eq(conteudo), eq("png")))
                .willReturn("https://api.example.com/uploads/foo.png");

        String url = service.atualizar(usuarioId, conteudo, "png");

        assertThat(url).isEqualTo("https://api.example.com/uploads/foo.png");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).salvar(captor.capture());
        assertThat(captor.getValue().fotoUrl()).isEqualTo("https://api.example.com/uploads/foo.png");
        assertThat(captor.getValue().email()).isEqualTo("ana@example.com");
    }

    @Test
    void deveLancarExcecaoQuandoOUsuarioNaoExiste() {
        UUID usuarioId = UUID.randomUUID();
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(usuarioId, new byte[] {1}, "png"))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }
}
