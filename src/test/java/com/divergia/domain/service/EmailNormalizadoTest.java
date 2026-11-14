package com.divergia.domain.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNormalizadoTest {

    @Test
    void minusculasSemEspacosNasPontas() {
        assertThat(EmailNormalizado.de("  Ana.Souza@Gmail.COM \n")).isEqualTo("ana.souza@gmail.com");
    }

    @Test
    void nuloViraVazio() {
        assertThat(EmailNormalizado.de(null)).isEmpty();
    }

    @Test
    void idempotente() {
        String uma = EmailNormalizado.de("A@B.com");
        assertThat(EmailNormalizado.de(uma)).isEqualTo(uma);
    }
}
