package com.divergia.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        Instant timestamp, int status, String error, String message, String path, String codigo, String campo) {

    public static ErroResponse of(HttpStatus status, String message, String path) {
        return new ErroResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path, null, null);
    }

    public static ErroResponse of(HttpStatus status, String message, String path, String codigo, String campo) {
        return new ErroResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path, codigo, campo);
    }
}
