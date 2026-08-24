package com.divergia.application.usecase;

import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.ResultadoAnalise;
import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.TrechoDeriva;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BuscarAnaliseServiceTest {

    @Mock
    private AnaliseRepositoryPort analiseRepository;

    @Mock
    private TrechoDerivaRepositoryPort trechoDerivaRepository;

    private BuscarAnaliseService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new BuscarAnaliseService(analiseRepository, trechoDerivaRepository);
    }

    @Test
    void deveBuscarAnaliseComSeusTrechosQuandoPertenceAoUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID analiseId = UUID.randomUUID();
        Analise analise = new Analise(analiseId, usuarioId, "original", "editado", true, Instant.now());
        TrechoDeriva trecho = new TrechoDeriva(
                UUID.randomUUID(), analiseId, "original", "editado", TipoDesvio.SENTIDO, "explicacao", 0.5, false);

        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.of(analise));
        given(trechoDerivaRepository.buscarPorAnaliseId(analiseId)).willReturn(List.of(trecho));

        ResultadoAnalise resultado = service.buscar(usuarioId, analiseId);

        assertThat(resultado.analise()).isEqualTo(analise);
        assertThat(resultado.trechosDeDeriva()).containsExactly(trecho);
    }

    @Test
    void deveLancarExcecaoQuandoAnaliseNaoExiste() {
        UUID usuarioId = UUID.randomUUID();
        UUID analiseId = UUID.randomUUID();
        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(usuarioId, analiseId))
                .isInstanceOf(AnaliseNaoEncontradaException.class);
    }

    @Test
    void deveLancarExcecaoQuandoAnalisePertenceAOutroUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID outroUsuarioId = UUID.randomUUID();
        UUID analiseId = UUID.randomUUID();
        Analise analise = new Analise(analiseId, outroUsuarioId, "original", "editado", true, Instant.now());

        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.of(analise));

        assertThatThrownBy(() -> service.buscar(usuarioId, analiseId))
                .isInstanceOf(AcessoNaoAutorizadoException.class);
    }
}
