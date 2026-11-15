package com.divergia.application.usecase;

import com.divergia.application.port.out.MetricasGlobaisPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.EstatisticasGlobais;
import com.divergia.domain.model.Papel;
import com.divergia.domain.model.PontoDiarioGlobal;
import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EstatisticasGlobaisServiceTest {

    // 15/11/2026 15:00 em Brasília.
    private static final Instant AGORA = Instant.parse("2026-11-15T18:00:00Z");

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private MetricasGlobaisPort metricas;

    private EstatisticasGlobaisService service;

    @BeforeEach
    void setUp() {
        service = new EstatisticasGlobaisService(usuarioRepository, metricas, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    private Usuario usuario(Papel papel) {
        return new Usuario(UUID.randomUUID(), "Ana", "ana@example.com", "hash", AGORA, null, papel);
    }

    private EstatisticasGlobais estatisticas(List<PontoDiarioGlobal> serie) {
        return new EstatisticasGlobais(
                42, 7, Map.of(TipoDesvio.SENTIDO, 5L, TipoDesvio.POSICAO, 2L), serie, AGORA);
    }

    @Test
    void deveNegarAcessoAUsuarioComum() {
        Usuario comum = usuario(Papel.USUARIO);
        given(usuarioRepository.buscarPorId(comum.id())).willReturn(Optional.of(comum));

        assertThatThrownBy(() -> service.obter(comum.id(), 30)).isInstanceOf(AcessoNaoAutorizadoException.class);

        verifyNoInteractions(metricas);
    }

    @Test
    void deveNegarAcessoQuandoOUsuarioNaoExiste() {
        UUID id = UUID.randomUUID();
        given(usuarioRepository.buscarPorId(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.obter(id, 30)).isInstanceOf(AcessoNaoAutorizadoException.class);

        verifyNoInteractions(metricas);
    }

    @Test
    void deveDevolverTotaisParaDevEPreencherOsDiasSemAnalise() {
        Usuario dev = usuario(Papel.DEV);
        given(usuarioRepository.buscarPorId(dev.id())).willReturn(Optional.of(dev));
        PontoDiarioGlobal ontem = new PontoDiarioGlobal(
                LocalDate.of(2026, 11, 14), 3, Map.of(TipoDesvio.SENTIDO, 2L));
        given(metricas.consultar(any(), any())).willReturn(estatisticas(List.of(ontem)));

        EstatisticasGlobais resultado = service.obter(dev.id(), 3);

        assertThat(resultado.totalAnalises()).isEqualTo(42);
        assertThat(resultado.derivasPorTipo().get(TipoDesvio.INTENSIDADE)).isZero();
        assertThat(resultado.serieDiaria()).extracting(PontoDiarioGlobal::dia).containsExactly(
                LocalDate.of(2026, 11, 13), LocalDate.of(2026, 11, 14), LocalDate.of(2026, 11, 15));
        assertThat(resultado.serieDiaria()).extracting(PontoDiarioGlobal::analises).containsExactly(0L, 3L, 0L);
        verify(metricas).consultar(LocalDate.of(2026, 11, 13), AGORA);
    }

    @Test
    void deveLimitarAJanelaDeDias() {
        Usuario dev = usuario(Papel.DEV);
        given(usuarioRepository.buscarPorId(dev.id())).willReturn(Optional.of(dev));
        given(metricas.consultar(any(), any())).willReturn(estatisticas(List.of()));

        assertThat(service.obter(dev.id(), 100_000).serieDiaria()).hasSize(EstatisticasGlobaisService.DIAS_MAXIMOS);
        assertThat(service.obter(dev.id(), -5).serieDiaria()).hasSize(EstatisticasGlobaisService.DIAS_MINIMOS);
    }
}
