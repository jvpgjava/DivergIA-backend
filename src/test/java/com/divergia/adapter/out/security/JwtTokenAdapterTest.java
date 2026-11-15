package com.divergia.adapter.out.security;

import com.divergia.domain.model.TokenAcesso;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenAdapterTest {

    private static final String SEGREDO = "chave-de-teste-com-pelo-menos-32-caracteres-para-hs256";

    private final JwtTokenAdapter adapter = new JwtTokenAdapter(new JwtProperties(SEGREDO));

    @Test
    void deveGerarUmTokenQueValidaDeVoltaParaOMesmoUsuario() {
        UUID usuarioId = UUID.randomUUID();

        TokenAcesso gerado = adapter.gerar(usuarioId);
        TokenAcesso validado = adapter.validar(gerado.valor());

        assertThat(validado.usuarioId()).isEqualTo(usuarioId);
        assertThat(validado.jti()).isEqualTo(gerado.jti());
    }

    @Test
    void tokenNovoNaoCarregaDataDeExpiracao() {
        TokenAcesso gerado = adapter.gerar(UUID.randomUUID());

        String payload = new String(
                Base64.getUrlDecoder().decode(gerado.valor().split("\\.")[1]), StandardCharsets.UTF_8);

        assertThat(payload).doesNotContain("\"exp\"");
        assertThat(adapter.validar(gerado.valor()).jti()).isEqualTo(gerado.jti());
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
                new JwtProperties("outra-chave-completamente-diferente-com-32-caracteres"));
        TokenAcesso gerado = outroEmissor.gerar(UUID.randomUUID());

        assertThatThrownBy(() -> adapter.validar(gerado.valor())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tokenAntigoQueAindaTemExpVencidoContinuaRejeitado() {
        UUID usuarioId = UUID.randomUUID();
        String legado = Jwts.builder()
                .subject(usuarioId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date(System.currentTimeMillis() - 3_600_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(SEGREDO.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> adapter.validar(legado)).isInstanceOf(IllegalArgumentException.class);
    }
}
