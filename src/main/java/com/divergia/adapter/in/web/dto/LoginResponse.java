package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.TokenAcesso;

import java.time.Instant;

public record LoginResponse(String accessToken, Instant expiraEm) {

    public static LoginResponse from(TokenAcesso token) {
        return new LoginResponse(token.valor(), token.expiraEm());
    }
}
