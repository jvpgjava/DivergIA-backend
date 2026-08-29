package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.Analise;
import com.divergia.domain.model.ResultadoAnalise;
import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.TrechoDeriva;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AnaliseResumoResponseTest {

    private Analise novaAnalise(String textoOriginal) {
        return new Analise(UUID.randomUUID(), UUID.randomUUID(), textoOriginal, "editado", textoOriginal != null, Instant.now());
    }

    private TrechoDeriva novoTrecho(UUID analiseId, TipoDesvio tipo, double intensidade) {
        return new TrechoDeriva(UUID.randomUUID(), analiseId, "original", "editado", tipo, "explicação", intensidade, false);
    }

    @Test
    void devePegarOTrechoDeMaiorIntensidadeComoPrincipal() {
        Analise analise = novaAnalise("o texto original completo");
        var resultado = new ResultadoAnalise(
                analise,
                List.of(
                        novoTrecho(analise.id(), TipoDesvio.SENTIDO, 0.4),
                        novoTrecho(analise.id(), TipoDesvio.INTENSIDADE, 0.9),
                        novoTrecho(analise.id(), TipoDesvio.POSICAO, 0.6)));

        AnaliseResumoResponse resumo = AnaliseResumoResponse.from(resultado);

        assertThat(resumo.pontuacaoIntensidade()).isEqualTo(90);
        assertThat(resumo.tipoDesvioPrincipal()).isEqualTo(TipoDesvio.INTENSIDADE);
    }

    @Test
    void deveDevolverPontuacaoETipoNulosQuandoNaoHaTrechos() {
        Analise analise = novaAnalise("texto original");
        var resultado = new ResultadoAnalise(analise, List.of());

        AnaliseResumoResponse resumo = AnaliseResumoResponse.from(resultado);

        assertThat(resumo.pontuacaoIntensidade()).isNull();
        assertThat(resumo.tipoDesvioPrincipal()).isNull();
    }

    @Test
    void deveDevolverPreviewNuloQuandoTextoNaoFoiRetido() {
        Analise analise = novaAnalise(null);
        var resultado = new ResultadoAnalise(analise, List.of());

        AnaliseResumoResponse resumo = AnaliseResumoResponse.from(resultado);

        assertThat(resumo.textoRetido()).isFalse();
        assertThat(resumo.textoPreview()).isNull();
    }

    @Test
    void deveTruncarPreviewMaiorQue140Caracteres() {
        String textoLongo = "a".repeat(200);
        Analise analise = novaAnalise(textoLongo);
        var resultado = new ResultadoAnalise(analise, List.of());

        AnaliseResumoResponse resumo = AnaliseResumoResponse.from(resultado);

        assertThat(resumo.textoPreview()).hasSize(141).endsWith("…");
    }

    @Test
    void naoDeveTruncarPreviewMenorQueOLimite() {
        Analise analise = novaAnalise("texto curto");
        var resultado = new ResultadoAnalise(analise, List.of());

        AnaliseResumoResponse resumo = AnaliseResumoResponse.from(resultado);

        assertThat(resumo.textoPreview()).isEqualTo("texto curto");
    }
}
