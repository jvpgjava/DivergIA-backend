package com.divergia.application.usecase;

import com.divergia.application.port.out.UsuarioRepositoryPort;
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
class ObterUsuarioLogadoServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private ObterUsuarioLogadoService service;

    @BeforeEach
    void setUp() {
        service = new ObterUsuarioLogadoService(usuarioRepository);
    }

    @Test
    void deveDevolverOUsuarioQuandoEleExiste() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario(usuarioId, "Ana Clara", "ana@example.com", "hash", Instant.now());
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.of(usuario));

        Usuario resultado = service.obter(usuarioId);

        assertThat(resultado).isEqualTo(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoOUsuarioNaoExisteMais() {
        UUID usuarioId = UUID.randomUUID();
        given(usuarioRepository.buscarPorId(usuarioId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.obter(usuarioId))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }
}
