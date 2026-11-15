package com.divergia.adapter.in.web.dto;

import com.divergia.domain.model.TokenAcesso;

public record LoginResponse(String accessToken) {

    public static LoginResponse from(TokenAcesso token) {
        return new LoginResponse(token.valor());
    }
}
