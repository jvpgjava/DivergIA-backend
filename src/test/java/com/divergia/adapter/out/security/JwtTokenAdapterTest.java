package com.divergia.adapter.out.security;

import com.divergia.domain.model.TokenAcesso;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenAdapterTest {

    private final JwtTokenAdapter adapter = new JwtTokenAdapter(
            new JwtProperties("chave-de-teste-com-pelo-menos-32-caracteres-para-hs256", 15));

    @Test
    void deveGerarUmTokenQueValidaDeVoltaParaOMesmoUsuario() {
        UUID usuarioId = UUID.randomUUID();

        TokenAcesso gerado = adapter.gerar(usuarioId);
        TokenAcesso validado = adapter.validar(gerado.valor());

        assertThat(validado.usuarioId()).isEqualTo(usuarioId);
        assertThat(validado.jti()).isEqualTo(gerado.jti());
    }

    @Test
    void deveRejeitarTokenComAssinaturaAdulterada() {
        TokenAcesso gerado = adapter.gerar(UUID.randomUUID());
        String adulterado = gerado.valor().substring(0, gerado.valor().length() - 4) + "abcd";

        assertThatThrownBy(() -> adapter.validar(adulterado)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarTokenAssinadoComOutraChave() {
        JwtTokenAdapter outroEmissor = new JwtTokenAdapter(
                new JwtProperties("outra-chave-completamente-diferente-com-32-caracteres", 15));
        TokenAcesso gerado = outroEmissor.gerar(UUID.randomUUID());

        assertThatThrownBy(() -> adapter.validar(gerado.valor())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtTokenAdapter adapterJaExpirado = new JwtTokenAdapter(
                new JwtProperties("chave-de-teste-com-pelo-menos-32-caracteres-para-hs256", -1));
        TokenAcesso gerado = adapterJaExpirado.gerar(UUID.randomUUID());

        assertThatThrownBy(() -> adapterJaExpirado.validar(gerado.valor()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
