package com.divergia.domain.service;

import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.TipoDesvio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuardaDeDerivasTest {

    private static final String ORIGINAL = "Gosto de caminhadas, corridas leves e outras modalidades ao ar livre. "
            + "Sou contra essa proposta.";
    private static final String EDITADO = "Aprecio opções como caminhadas e corridas moderadas ao ar livre. "
            + "Essa proposta tem pontos que merecem ser considerados.";

    private final GuardaDeDerivas guarda = new GuardaDeDerivas();

    private static AvaliacaoDeDeriva deriva(TipoDesvio tipo, String original, String editado, double intensidade) {
        return new AvaliacaoDeDeriva(tipo, original, editado, "A força da afirmação mudou além do estilo.", intensidade);
    }

    @Test
    void mantemDerivaComCitacoesQueExistemNosTextos() {
        var resultado = guarda.filtrar(ORIGINAL, EDITADO, List.of(
                deriva(TipoDesvio.INTENSIDADE, "corridas leves", "corridas moderadas", 0.4)));

        assertThat(resultado.aprovadas()).hasSize(1);
        assertThat(resultado.totalDescartado()).isZero();
    }

    @Test
    void descartaCitacaoInventadaNoOriginal() {
        var resultado = guarda.filtrar(ORIGINAL, EDITADO, List.of(
                deriva(TipoDesvio.SENTIDO, "eu odeio esportes", "corridas moderadas", 0.8)));

        assertThat(resultado.aprovadas()).isEmpty();
        assertThat(resultado.descartes()).containsKey(GuardaDeDerivas.Descarte.CITACAO_ORIGINAL_INEXISTENTE);
    }

    @Test
    void descartaCitacaoInventadaNoEditado() {
        var resultado = guarda.filtrar(ORIGINAL, EDITADO, List.of(
                deriva(TipoDesvio.POSICAO, "Sou contra essa proposta.", "Apoio totalmente a proposta.", 0.9)));

        assertThat(resultado.aprovadas()).isEmpty();
        assertThat(resultado.descartes()).containsKey(GuardaDeDerivas.Descarte.CITACAO_EDITADA_INEXISTENTE);
    }

    @Test
    void ancoraCitacaoComCaixaAspasEEspacosDiferentesNaFormaExataDoTexto() {
        var resultado = guarda.filtrar(ORIGINAL, EDITADO, List.of(
                deriva(TipoDesvio.POSICAO, "\"sou   CONTRA essa proposta\"",
                        "“essa proposta tem pontos que merecem ser considerados”", 0.75)));

        assertThat(resultado.aprovadas()).singleElement().satisfies(d -> {
            assertThat(d.trechoOriginal()).isEqualTo("Sou contra essa proposta");
            assertThat(d.trechoEditado()).isEqualTo("Essa proposta tem pontos que merecem ser considerados");
        });
    }

    @Test
    void aceitaCitacaoComUmaPalavraTrocadaPorJanelaDePalavras() {
        var resultado = guarda.filtrar(ORIGINAL, EDITADO, List.of(
                deriva(TipoDesvio.INTENSIDADE, "caminhadas, corridas leves e outras atividades ao ar livre",
                        "caminhadas e corridas moderadas ao ar livre", 0.4)));

        assertThat(resultado.aprovadas()).hasSize(1);
    }

    @Test
    void descartaDerivaSemMudancaReal() {
        String original = "O relatório ficou pronto ontem. A equipe aprovou o texto.";
        String editado = "O relatório ficou pronto ontem. A equipe adorou o texto.";

        var resultado = guarda.filtrar(original, editado, List.of(
                deriva(TipoDesvio.SENTIDO, "O relatório ficou pronto ontem.", "O relatório ficou pronto ontem.", 0.5)));

        assertThat(resultado.aprovadas()).isEmpty();
        assertThat(resultado.descartes()).containsKey(GuardaDeDerivas.Descarte.SEM_MUDANCA);
    }

    @Test
    void descartaIntensidadeIrrelevante() {
        var resultado = guarda.filtrar(ORIGINAL, EDITADO, List.of(
                deriva(TipoDesvio.INTENSIDADE, "corridas leves", "corridas moderadas", 0.01)));

        assertThat(resultado.descartes()).containsKey(GuardaDeDerivas.Descarte.INTENSIDADE_IRRELEVANTE);
    }

    @Test
    void sobrepostasViramUmaSoComAMaisIntensa() {
        var resultado = guarda.filtrar(ORIGINAL, EDITADO, List.of(
                deriva(TipoDesvio.INTENSIDADE, "corridas leves", "corridas moderadas", 0.3),
                deriva(TipoDesvio.SENTIDO, "caminhadas, corridas leves", "caminhadas e corridas moderadas", 0.6)));

        assertThat(resultado.aprovadas()).singleElement()
                .extracting(AvaliacaoDeDeriva::tipoDesvio).isEqualTo(TipoDesvio.SENTIDO);
        assertThat(resultado.descartes()).containsEntry(GuardaDeDerivas.Descarte.SOBREPOSTA, 1);
    }

    @Test
    void devolveNaOrdemDoTextoEditado() {
        var resultado = guarda.filtrar(ORIGINAL, EDITADO, List.of(
                deriva(TipoDesvio.POSICAO, "Sou contra essa proposta.",
                        "Essa proposta tem pontos que merecem ser considerados.", 0.75),
                deriva(TipoDesvio.INTENSIDADE, "corridas leves", "corridas moderadas", 0.4)));

        assertThat(resultado.aprovadas()).extracting(AvaliacaoDeDeriva::tipoDesvio)
                .containsExactly(TipoDesvio.INTENSIDADE, TipoDesvio.POSICAO);
    }

    @Test
    void limitaAQuantidadeDeDerivas() {
        StringBuilder original = new StringBuilder();
        StringBuilder editado = new StringBuilder();
        List<AvaliacaoDeDeriva> candidatas = new ArrayList<>();
        for (int i = 0; i < GuardaDeDerivas.MAXIMO_DERIVAS + 3; i++) {
            original.append("A frase número ").append(i).append(" é calma. ");
            editado.append("A frase número ").append(i).append(" é furiosa. ");
            candidatas.add(deriva(TipoDesvio.INTENSIDADE, "frase número " + i + " é calma",
                    "frase número " + i + " é furiosa", 0.5));
        }

        var resultado = guarda.filtrar(original.toString(), editado.toString(), candidatas);

        assertThat(resultado.aprovadas()).hasSize(GuardaDeDerivas.MAXIMO_DERIVAS);
        assertThat(resultado.descartes()).containsEntry(GuardaDeDerivas.Descarte.EXCEDENTE, 3);
    }

    @Test
    void trocaExplicacaoQueFalaDoFuncionamentoInternoPorUmaMontadaComAsCitacoes() {
        var resultado = guarda.filtrar(ORIGINAL, EDITADO, List.of(new AvaliacaoDeDeriva(
                TipoDesvio.INTENSIDADE, "corridas leves", "corridas moderadas",
                "Segundo os exemplos de referência do prompt, isso é uma deriva.", 0.4)));

        assertThat(resultado.explicacoesTrocadas()).isEqualTo(1);
        assertThat(resultado.aprovadas().get(0).explicacao())
                .contains("corridas leves").contains("corridas moderadas").doesNotContain("prompt");
    }

    @Test
    void trocaExplicacaoQueIntroduzOfensa() {
        var resultado = guarda.filtrar(ORIGINAL, EDITADO, List.of(new AvaliacaoDeDeriva(
                TipoDesvio.INTENSIDADE, "corridas leves", "corridas moderadas",
                "A IA fez uma merda e aumentou a força da frase.", 0.4)));

        assertThat(resultado.aprovadas().get(0).explicacao()).doesNotContain("merda");
    }

    @Test
    void mantemExplicacaoQueCitaTermoQueVeioDoProprioTextoDoUsuario() {
        String original = "Usei o GPT para revisar e ele foi útil.";
        String editado = "Usei o GPT para revisar e ele foi indispensável.";

        var resultado = guarda.filtrar(original, editado, List.of(new AvaliacaoDeDeriva(
                TipoDesvio.INTENSIDADE, "ele foi útil", "ele foi indispensável",
                "O texto dizia que o GPT foi útil; a edição diz que foi indispensável.", 0.5)));

        assertThat(resultado.explicacoesTrocadas()).isZero();
    }

    @Test
    void limpaMarkdownDaExplicacao() {
        var resultado = guarda.filtrar(ORIGINAL, EDITADO, List.of(new AvaliacaoDeDeriva(
                TipoDesvio.INTENSIDADE, "corridas leves", "corridas moderadas",
                "**Leves** virou `moderadas`, o que aumenta a intensidade.", 0.4)));

        assertThat(resultado.aprovadas().get(0).explicacao())
                .isEqualTo("Leves virou moderadas, o que aumenta a intensidade.");
    }
}
