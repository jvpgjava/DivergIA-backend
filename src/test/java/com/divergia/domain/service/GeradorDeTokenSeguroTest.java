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
    }

    @Test
    void deveGerarCodigoDeSeisCaracteresSemLetrasOuDigitosAmbiguos() {
        String codigo = gerador.gerar();

        assertThat(codigo).hasSize(6);
        assertThat(codigo).matches("[A-HJ-NP-Z2-9]{6}");
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
