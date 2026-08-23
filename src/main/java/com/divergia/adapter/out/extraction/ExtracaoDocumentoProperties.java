package com.divergia.adapter.out.extraction;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "divergia.extracao")
public record ExtracaoDocumentoProperties(String baseUrl, long timeoutSegundos) {
}
