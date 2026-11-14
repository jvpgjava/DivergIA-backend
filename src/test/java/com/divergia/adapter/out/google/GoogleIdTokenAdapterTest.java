package com.divergia.adapter.out.google;

import com.divergia.domain.model.IdentidadeGoogle;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleIdTokenAdapterTest {

    private static final String CLIENT_ID = "123-web.apps.googleusercontent.com";
    private static KeyPair chavesDoGoogle;
    private static KeyPair chavesDeUmAtacante;

    @BeforeAll
    static void gerarChaves() throws Exception {
        KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
        gerador.initialize(2048);
        chavesDoGoogle = gerador.generateKeyPair();
        chavesDeUmAtacante = gerador.generateKeyPair();
    }

    private static GoogleIdTokenAdapter adapter() {
        return new GoogleIdTokenAdapter(CLIENT_ID,
                NimbusJwtDecoder.withPublicKey((RSAPublicKey) chavesDoGoogle.getPublic()).build());
    }

    private static String token(KeyPair chaves, Map<String, Object> sobrescritas) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .issuer("https://accounts.google.com")
                .audience(CLIENT_ID)
                .subject("10987654321")
                .expirationTime(Date.from(Instant.now().plusSeconds(600)))
                .issueTime(new Date())
                .claim("email", "ana@gmail.com")
                .claim("email_verified", true)
                .claim("name", "Ana Souza");
        sobrescritas.forEach(claims::claim);
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims.build());
        jwt.sign(new RSASSASigner(chaves.getPrivate()));
        return jwt.serialize();
    }

    @Test
    void tokenValidoDevolveAIdentidade() throws Exception {
        Optional<IdentidadeGoogle> identidade = adapter().verificar(token(chavesDoGoogle, Map.of()));

        assertThat(identidade).contains(new IdentidadeGoogle("10987654321", "ana@gmail.com", "Ana Souza"));
    }

    @Test
    void assinaturaDeOutraChaveERecusada() throws Exception {
        assertThat(adapter().verificar(token(chavesDeUmAtacante, Map.of()))).isEmpty();
    }

    @Test
    void tokenEmitidoParaOutroAppERecusado() throws Exception {
        assertThat(adapter().verificar(token(chavesDoGoogle, Map.of("aud", "outro-app.apps.googleusercontent.com"))))
                .isEmpty();
    }

    @Test
    void emissorQueNaoEOGoogleERecusado() throws Exception {
        assertThat(adapter().verificar(token(chavesDoGoogle, Map.of("iss", "https://evil.example.com")))).isEmpty();
    }

    @Test
    void tokenExpiradoERecusado() throws Exception {
        assertThat(adapter().verificar(token(chavesDoGoogle,
                Map.of("exp", Date.from(Instant.now().minusSeconds(3600)))))).isEmpty();
    }

    @Test
    void emailNaoVerificadoERecusado() throws Exception {
        assertThat(adapter().verificar(token(chavesDoGoogle, Map.of("email_verified", false)))).isEmpty();
    }

    @Test
    void lixoNoLugarDoTokenERecusadoSemExcecao() {
        assertThat(adapter().verificar("isto.nao.e-um-jwt")).isEmpty();
    }

    @Test
    void semClientIdConfiguradoOLoginFicaDesligado() {
        assertThat(new GoogleIdTokenAdapter("", NimbusJwtDecoder.withPublicKey(
                (RSAPublicKey) chavesDoGoogle.getPublic()).build()).configurado()).isFalse();
        assertThat(adapter().configurado()).isTrue();
    }
}
