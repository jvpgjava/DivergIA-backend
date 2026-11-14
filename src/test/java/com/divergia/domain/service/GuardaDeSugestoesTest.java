package com.divergia.domain.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuardaDeSugestoesTest {

    private static final String ORIGINAL = "Sou contra essa proposta.";
    private static final String EDITADO = "Essa proposta tem pontos que merecem ser considerados.";

    private final GuardaDeSugestoes guarda = new GuardaDeSugestoes();

    @Test
    void limpaNumeracaoAspasERotulos() {
        var r = guarda.filtrar(ORIGINAL, EDITADO, List.of(
                "1. \"Não apoio essa proposta.\"",
                "Alternativa 2: Sou contrário a essa proposta.",
                "- **Discordo dessa proposta.**"));

        assertThat(r.aprovadas()).containsExactly(
                "Não apoio essa proposta.", "Sou contrário a essa proposta.", "Discordo dessa proposta.");
    }

    @Test
    void descartaCopiaDoEditadoDuplicadaEVazia() {
        var r = guarda.filtrar(ORIGINAL, EDITADO, List.of(
                EDITADO, "Não apoio essa proposta.", "não apoio essa   proposta.", "   "));

        assertThat(r.aprovadas()).containsExactly("Não apoio essa proposta.");
        assertThat(r.descartes()).containsKeys(
                GuardaDeSugestoes.Descarte.COPIA_DO_EDITADO, GuardaDeSugestoes.Descarte.DUPLICADA,
                GuardaDeSugestoes.Descarte.VAZIA);
    }

    @Test
    void descartaMetacomentario() {
        var r = guarda.filtrar(ORIGINAL, EDITADO, List.of("Claro! Aqui estão: Sou contra essa ideia."));

        assertThat(r.aprovadas()).isEmpty();
        assertThat(r.descartes()).containsKey(GuardaDeSugestoes.Descarte.METACOMENTARIO);
    }

    @Test
    void naoTrataComoMetacomentarioQuandoOOriginalComecaIgual() {
        var r = guarda.filtrar("Claro que sou contra essa proposta.", EDITADO,
                List.of("Claro que não apoio essa proposta."));

        assertThat(r.aprovadas()).hasSize(1);
    }

    @Test
    void descartaOutroIdioma() {
        var r = guarda.filtrar(
                "Eu não concordo com essa proposta e vou votar contra ela.", EDITADO,
                List.of("I do not agree with this proposal and I will vote against it."));

        assertThat(r.descartes()).containsKey(GuardaDeSugestoes.Descarte.OUTRO_IDIOMA);
    }

    @Test
    void descartaOfensaQueNaoEstavaNoOriginal() {
        var r = guarda.filtrar(ORIGINAL, EDITADO, List.of("Sou contra essa porra de proposta."));

        assertThat(r.descartes()).containsKey(GuardaDeSugestoes.Descarte.OFENSA_INTRODUZIDA);
    }

    @Test
    void mantemOfensaQueJaEstavaNoOriginal() {
        var r = guarda.filtrar("Essa proposta é uma merda.", "Essa proposta pode melhorar.",
                List.of("Essa proposta é uma merda completa."));

        assertThat(r.aprovadas()).hasSize(1);
    }

    @Test
    void descartaSugestaoLongaDemais() {
        var r = guarda.filtrar(ORIGINAL, EDITADO, List.of("Sou contra essa proposta porque ".repeat(20)));

        assertThat(r.descartes()).containsKey(GuardaDeSugestoes.Descarte.LONGA_DEMAIS);
    }

    @Test
    void limitaATresSugestoes() {
        var r = guarda.filtrar(ORIGINAL, EDITADO, List.of(
                "Não apoio essa proposta.", "Discordo dessa proposta.", "Sou contrário a essa proposta.",
                "Rejeito essa proposta."));

        assertThat(r.aprovadas()).hasSize(GuardaDeSugestoes.MAXIMO_SUGESTOES);
    }
}
