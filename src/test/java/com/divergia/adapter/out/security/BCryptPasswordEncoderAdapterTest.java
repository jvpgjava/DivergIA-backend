package com.divergia.adapter.out.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordEncoderAdapterTest {

    private final BCryptPasswordEncoderAdapter adapter = new BCryptPasswordEncoderAdapter();

    @Test
    void deveConferirSenhaCorretaAposCodificar() {
        String hash = adapter.codificar("senha12345");

        assertThat(hash).isNotEqualTo("senha12345");
        assertThat(adapter.confere("senha12345", hash)).isTrue();
    }

    @Test
    void naoDeveConferirSenhaIncorreta() {
        String hash = adapter.codificar("senha12345");

        assertThat(adapter.confere("senha-errada", hash)).isFalse();
    }
}
