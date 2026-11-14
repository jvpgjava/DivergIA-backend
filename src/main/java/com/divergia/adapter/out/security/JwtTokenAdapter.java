package com.divergia.adapter.out.security;

import com.divergia.application.port.out.TokenPort;
import com.divergia.domain.model.TokenAcesso;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtTokenAdapter implements TokenPort {

    private final SecretKey chave;
    private final Duration expiracao;

    public JwtTokenAdapter(JwtProperties properties) {
        this.chave = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expiracao = Duration.ofMinutes(properties.expiracaoMinutos());
    }

    @Override
    public TokenAcesso gerar(UUID usuarioId) {
        String jti = UUID.randomUUID().toString();
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(expiracao);

        String valor = Jwts.builder()
                .subject(usuarioId.toString())
                .id(jti)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiraEm))
                .signWith(chave)
                .compact();

        return new TokenAcesso(valor, jti, usuarioId, expiraEm);
    }

    @Override
    public TokenAcesso validar(String tokenBruto) {
        try {
            Jws<Claims> jws = Jwts.parser().verifyWith(chave).build().parseSignedClaims(tokenBruto);
            Claims claims = jws.getPayload();
            return new TokenAcesso(
                    tokenBruto,
                    claims.getId(),
                    UUID.fromString(claims.getSubject()),
                    claims.getExpiration().toInstant());
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Token de acesso inválido ou expirado", e);
        }
    }
}
