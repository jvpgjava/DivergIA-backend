package com.divergia.adapter.out.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "divergia.jwt")
public record JwtProperties(String secret, long expiracaoMinutos) {
}
