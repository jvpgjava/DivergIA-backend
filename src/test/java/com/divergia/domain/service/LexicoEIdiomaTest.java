package com.divergia.domain.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LexicoEIdiomaTest {

    @Test
    void detectaIdiomasComunsComTextoSuficiente() {
        assertThat(DetectorDeIdioma.detectar("Eu acho que isso não é uma boa ideia para a nossa equipe."))
                .contains("pt");
        assertThat(DetectorDeIdioma.detectar("I think this is not a good idea for our team at all."))
                .contains("en");
        assertThat(DetectorDeIdioma.detectar("Creo que esto no es una buena idea para el equipo y los clientes."))
                .contains("es");
    }

    @Test
    void naoArriscaComPoucasPalavras() {
        assertThat(DetectorDeIdioma.detectar("Sou contra.")).isEmpty();
        assertThat(DetectorDeIdioma.detectar("Deploy rápido!")).isEmpty();
    }

    @Test
    void portuguesComTermosEmInglesContinuaPortugues() {
        assertThat(DetectorDeIdioma.detectar(
                "O feedback do cliente foi que o deadline está muito apertado para a nossa equipe."))
                .contains("pt");
    }

    @Test
    void encontraOfensaComLeetERepeticao() {
        assertThat(LexicoOfensivo.encontrar("que p0rr4aaaa é essa")).containsExactly("porra");
        assertThat(LexicoOfensivo.encontrar("o viaduto caiu e o computador travou")).isEmpty();
    }

    @Test
    void opiniaoComPalavraoNaoEhSoOfensa() {
        assertThat(LexicoOfensivo.soOfensa("Esse projeto é uma merda e ninguém da equipe liga para os prazos."))
                .isFalse();
        assertThat(LexicoOfensivo.soOfensa("seu idiota babaca")).isTrue();
    }

    @Test
    void introduzidosIgnoraOQueJaEstavaNaFonte() {
        assertThat(LexicoOfensivo.introduzidos("isso é uma merda", "que merda de dia")).isEmpty();
        assertThat(LexicoOfensivo.introduzidos("isso é uma merda", "que dia ruim")).containsExactly("merda");
    }

    @Test
    void identicosSoIgnoraEspacos() {
        assertThat(NormalizadorDeTexto.identicos("Ele veio.", "  Ele   veio. ")).isTrue();
        assertThat(NormalizadorDeTexto.identicos("Ele veio.", "Ele veio?")).isFalse();
        assertThat(NormalizadorDeTexto.identicos("ele é", "ele e")).isFalse();
    }

    @Test
    void limparRemoveInvisiveisEPreservaOTexto() {
        assertThat(NormalizadorDeTexto.limpar("Olá​, mundo﻿!\r\n\r\n\r\nFim"))
                .isEqualTo("Olá, mundo!\n\nFim");
    }
}
