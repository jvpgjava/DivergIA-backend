package com.divergia.application.usecase;

import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.domain.model.Analise;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExcluirAnaliseServiceTest {

    @Mock
    private AnaliseRepositoryPort analiseRepository;

    private ExcluirAnaliseService service;

    @BeforeEach
    void setUp() {
        service = new ExcluirAnaliseService(analiseRepository);
    }

    @Test
    void deveExcluirAnaliseQuandoPertenceAoUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID analiseId = UUID.randomUUID();
        Analise analise = new Analise(analiseId, usuarioId, "original", "editado", true, Instant.now());
        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.of(analise));

        service.excluir(usuarioId, analiseId);

        verify(analiseRepository).excluir(analiseId);
    }

    @Test
    void deveLancarExcecaoQuandoAnaliseNaoExiste() {
        UUID usuarioId = UUID.randomUUID();
        UUID analiseId = UUID.randomUUID();
        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(usuarioId, analiseId))
                .isInstanceOf(AnaliseNaoEncontradaException.class);
        verify(analiseRepository, never()).excluir(any());
    }

    @Test
    void deveLancarExcecaoQuandoAnalisePertenceAOutroUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID outroUsuarioId = UUID.randomUUID();
        UUID analiseId = UUID.randomUUID();
        Analise analise = new Analise(analiseId, outroUsuarioId, "original", "editado", true, Instant.now());
        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.of(analise));

        assertThatThrownBy(() -> service.excluir(usuarioId, analiseId))
                .isInstanceOf(AcessoNaoAutorizadoException.class);
        verify(analiseRepository, never()).excluir(any());
    }
}
