package com.divergia.domain.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeradorDeTokenSeguroTest {

    private final GeradorDeTokenSeguro gerador = new GeradorDeTokenSeguro();

    @Test
    void deveGerarTokensDiferentesACadaChamada() {
        String primeiro = gerador.gerar();
        String segundo = gerador.gerar();

        assertThat(primeiro).isNotEqualTo(segundo);
        assertThat(primeiro).hasSizeGreaterThan(30);
    }

    @Test
    void deveProduzirOMesmoHashParaOMesmoTokenBruto() {
        String token = gerador.gerar();

        assertThat(gerador.hash(token)).isEqualTo(gerador.hash(token));
    }

    @Test
    void deveProduzirHashesDiferentesParaTokensDiferentes() {
        String tokenUm = gerador.gerar();
        String tokenDois = gerador.gerar();

        assertThat(gerador.hash(tokenUm)).isNotEqualTo(gerador.hash(tokenDois));
    }
}
