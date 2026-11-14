package com.divergia.adapter.out.google;

import com.divergia.application.port.out.VerificadorGooglePort;
import com.divergia.domain.model.IdentidadeGoogle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class GoogleIdTokenAdapter implements VerificadorGooglePort {

    private static final Logger log = LoggerFactory.getLogger(GoogleIdTokenAdapter.class);
    private static final String CHAVES_PUBLICAS_GOOGLE = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> EMISSORES = Set.of("accounts.google.com", "https://accounts.google.com");

    private final List<String> clientIds;
    private final JwtDecoder decoder;

    @Autowired
    public GoogleIdTokenAdapter(@Value("${divergia.google.client-ids:}") String clientIds) {
        this(clientIds, NimbusJwtDecoder.withJwkSetUri(CHAVES_PUBLICAS_GOOGLE).build());
    }

    GoogleIdTokenAdapter(String clientIds, JwtDecoder decoder) {
        this.clientIds = Arrays.stream(clientIds == null ? new String[0] : clientIds.split(","))
                .map(String::strip)
                .filter(id -> !id.isEmpty())
                .toList();
        this.decoder = decoder;
        if (decoder instanceof NimbusJwtDecoder nimbus) {
            nimbus.setJwtValidator(validador(this.clientIds));
        }
    }

    @Override
    public boolean configurado() {
        return !clientIds.isEmpty();
    }

    @Override
    public Optional<IdentidadeGoogle> verificar(String idToken) {
        Jwt jwt;
        try {
            jwt = decoder.decode(idToken);
        } catch (JwtException e) {
            log.info("ID token do Google recusado: {}", e.getClass().getSimpleName());
            return Optional.empty();
        }
        if (!Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
            return Optional.empty();
        }
        String email = jwt.getClaimAsString("email");
        if (email == null || !email.contains("@")) {
            return Optional.empty();
        }
        return Optional.of(new IdentidadeGoogle(jwt.getSubject(), email, jwt.getClaimAsString("name")));
    }

    static OAuth2TokenValidator<Jwt> validador(List<String> clientIds) {
        OAuth2TokenValidator<Jwt> emissor = jwt -> EMISSORES.contains(jwt.getClaimAsString("iss"))
                ? OAuth2TokenValidatorResult.success()
                : falha("emissor inválido");
        // O token precisa ter sido emitido para o nosso app: token de outro app não pode entrar aqui.
        OAuth2TokenValidator<Jwt> destinatario = jwt -> jwt.getAudience() != null
                && jwt.getAudience().stream().anyMatch(clientIds::contains)
                ? OAuth2TokenValidatorResult.success()
                : falha("destinatário inválido");
        return new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), emissor, destinatario);
    }

    private static OAuth2TokenValidatorResult falha(String descricao) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", descricao, null));
    }
}
