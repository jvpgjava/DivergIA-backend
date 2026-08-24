package com.divergia.application.usecase;

import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.PainelTendencia;
import com.divergia.domain.model.PontoTendencia;
import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.TrechoDeriva;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PainelTendenciaServiceTest {

    @Mock
    private AnaliseRepositoryPort analiseRepository;

    @Mock
    private TrechoDerivaRepositoryPort trechoDerivaRepository;

    private PainelTendenciaService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new PainelTendenciaService(analiseRepository, trechoDerivaRepository);
    }

    @Test
    void deveAgregarTotaisDistribuicaoPorTipoEEvolucaoMensal() {
        UUID usuarioId = UUID.randomUUID();

        Instant janeiro = Instant.parse("2026-01-15T10:00:00Z");
        Instant fevereiro = Instant.parse("2026-02-10T10:00:00Z");

        UUID analiseJaneiro1 = UUID.randomUUID();
        UUID analiseJaneiro2 = UUID.randomUUID();
        UUID analiseFevereiro = UUID.randomUUID();

        List<Analise> analises = List.of(
                new Analise(analiseJaneiro1, usuarioId, "a", "b", true, janeiro),
                new Analise(analiseJaneiro2, usuarioId, "c", "d", true, janeiro.plusSeconds(60)),
                new Analise(analiseFevereiro, usuarioId, "e", "f", true, fevereiro));

        List<TrechoDeriva> trechos = List.of(
                new TrechoDeriva(
                        UUID.randomUUID(), analiseJaneiro1, "a", "b", TipoDesvio.SENTIDO, "exp", 0.8, false),
                new TrechoDeriva(
                        UUID.randomUUID(), analiseJaneiro2, "c", "d", TipoDesvio.SENTIDO, "exp", 0.4, false),
                new TrechoDeriva(
                        UUID.randomUUID(), analiseFevereiro, "e", "f", TipoDesvio.INTENSIDADE, "exp", 0.6, false));

        given(analiseRepository.buscarPorUsuarioId(usuarioId)).willReturn(analises);
        given(trechoDerivaRepository.buscarPorUsuarioId(usuarioId)).willReturn(trechos);

        PainelTendencia painel = service.obter(usuarioId);

        assertThat(painel.totalAnalises()).isEqualTo(3);
        assertThat(painel.totalDerivas()).isEqualTo(3);
        assertThat(painel.intensidadeMedia()).isCloseTo(0.6, offset(0.001));
        assertThat(painel.derivasPorTipo().get(TipoDesvio.SENTIDO)).isEqualTo(2);
        assertThat(painel.derivasPorTipo().get(TipoDesvio.INTENSIDADE)).isEqualTo(1);
        assertThat(painel.derivasPorTipo().get(TipoDesvio.POSICAO)).isEqualTo(0);

        assertThat(painel.evolucaoMensal()).hasSize(2);
        PontoTendencia pontoJaneiro = painel.evolucaoMensal().get(0);
        assertThat(pontoJaneiro.mes()).isEqualTo(YearMonth.of(2026, 1));
        assertThat(pontoJaneiro.quantidadeAnalises()).isEqualTo(2);
        assertThat(pontoJaneiro.quantidadeDerivas()).isEqualTo(2);
        assertThat(pontoJaneiro.intensidadeMedia()).isCloseTo(0.6, offset(0.001));

        PontoTendencia pontoFevereiro = painel.evolucaoMensal().get(1);
        assertThat(pontoFevereiro.mes()).isEqualTo(YearMonth.of(2026, 2));
        assertThat(pontoFevereiro.quantidadeAnalises()).isEqualTo(1);
        assertThat(pontoFevereiro.quantidadeDerivas()).isEqualTo(1);
    }

    @Test
    void deveIncluirMesDeAnaliseSemNenhumaDerivaDetectada() {
        UUID usuarioId = UUID.randomUUID();
        Instant marco = Instant.parse("2026-03-05T10:00:00Z");
        Analise analiseSemDeriva = new Analise(UUID.randomUUID(), usuarioId, "a", "a", true, marco);

        given(analiseRepository.buscarPorUsuarioId(usuarioId)).willReturn(List.of(analiseSemDeriva));
        given(trechoDerivaRepository.buscarPorUsuarioId(usuarioId)).willReturn(List.of());

        PainelTendencia painel = service.obter(usuarioId);

        assertThat(painel.evolucaoMensal()).hasSize(1);
        assertThat(painel.evolucaoMensal().get(0).mes()).isEqualTo(YearMonth.of(2026, 3));
        assertThat(painel.evolucaoMensal().get(0).quantidadeAnalises()).isEqualTo(1);
        assertThat(painel.evolucaoMensal().get(0).quantidadeDerivas()).isEqualTo(0);
    }

    @Test
    void deveDevolverPainelVazioQuandoUsuarioNaoTemAnalises() {
        UUID usuarioId = UUID.randomUUID();
        given(analiseRepository.buscarPorUsuarioId(usuarioId)).willReturn(List.of());
        given(trechoDerivaRepository.buscarPorUsuarioId(usuarioId)).willReturn(List.of());

        PainelTendencia painel = service.obter(usuarioId);

        assertThat(painel.totalAnalises()).isZero();
        assertThat(painel.totalDerivas()).isZero();
        assertThat(painel.intensidadeMedia()).isZero();
        assertThat(painel.evolucaoMensal()).isEmpty();
        assertThat(painel.derivasPorTipo().values()).allMatch(v -> v == 0);
    }
}
