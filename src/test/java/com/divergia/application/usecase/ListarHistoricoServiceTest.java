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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ListarHistoricoServiceTest {

    @Mock
    private AnaliseRepositoryPort analiseRepository;

    @Mock
    private TrechoDerivaRepositoryPort trechoDerivaRepository;

    @Test
    void deveListarAnalisesDoUsuarioOrdenadasPorDataDecrescente() {
        UUID usuarioId = UUID.randomUUID();
        Instant agora = Instant.now();
        Analise maisAntiga = new Analise(UUID.randomUUID(), usuarioId, "a", "b", true, agora.minusSeconds(3600));
        Analise maisRecente = new Analise(UUID.randomUUID(), usuarioId, "c", "d", true, agora);

        given(analiseRepository.buscarPorUsuarioId(usuarioId)).willReturn(List.of(maisAntiga, maisRecente));
        given(trechoDerivaRepository.buscarPorUsuarioId(usuarioId)).willReturn(List.of());

        ListarHistoricoService service = new ListarHistoricoService(analiseRepository, trechoDerivaRepository);
        List<ResultadoAnalise> resultado = service.listar(usuarioId);

        assertThat(resultado).extracting(ResultadoAnalise::analise).containsExactly(maisRecente, maisAntiga);
    }

    @Test
    void deveAgruparOsTrechosDeDerivaSobACadaAnaliseCorrespondente() {
        UUID usuarioId = UUID.randomUUID();
        Analise analise1 = new Analise(UUID.randomUUID(), usuarioId, "a", "b", true, Instant.now());
        Analise analise2 = new Analise(UUID.randomUUID(), usuarioId, "c", "d", true, Instant.now());
        TrechoDeriva trechoDaAnalise1 = new TrechoDeriva(
                UUID.randomUUID(), analise1.id(), "a", "b", TipoDesvio.SENTIDO, "explicação", 0.7, false);
        TrechoDeriva trechoDaAnalise2 = new TrechoDeriva(
                UUID.randomUUID(), analise2.id(), "c", "d", TipoDesvio.POSICAO, "explicação", 0.3, false);

        given(analiseRepository.buscarPorUsuarioId(usuarioId)).willReturn(List.of(analise1, analise2));
        given(trechoDerivaRepository.buscarPorUsuarioId(usuarioId))
                .willReturn(List.of(trechoDaAnalise1, trechoDaAnalise2));

        ListarHistoricoService service = new ListarHistoricoService(analiseRepository, trechoDerivaRepository);
        List<ResultadoAnalise> resultado = service.listar(usuarioId);

        ResultadoAnalise resumo1 = resultado.stream()
                .filter(r -> r.analise().id().equals(analise1.id()))
                .findFirst()
                .orElseThrow();
        ResultadoAnalise resumo2 = resultado.stream()
                .filter(r -> r.analise().id().equals(analise2.id()))
                .findFirst()
                .orElseThrow();

        assertThat(resumo1.trechosDeDeriva()).containsExactly(trechoDaAnalise1);
        assertThat(resumo2.trechosDeDeriva()).containsExactly(trechoDaAnalise2);
    }

    @Test
    void deveDevolverListaDeTrechosVaziaQuandoAnaliseNaoTemDerivas() {
        UUID usuarioId = UUID.randomUUID();
        Analise analise = new Analise(UUID.randomUUID(), usuarioId, "a", "b", true, Instant.now());

        given(analiseRepository.buscarPorUsuarioId(usuarioId)).willReturn(List.of(analise));
        given(trechoDerivaRepository.buscarPorUsuarioId(usuarioId)).willReturn(List.of());

        ListarHistoricoService service = new ListarHistoricoService(analiseRepository, trechoDerivaRepository);
        List<ResultadoAnalise> resultado = service.listar(usuarioId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).trechosDeDeriva()).isEmpty();
    }
}
