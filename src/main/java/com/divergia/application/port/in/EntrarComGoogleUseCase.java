package com.divergia.application.port.in;

import com.divergia.domain.model.TokenAcesso;

public interface EntrarComGoogleUseCase {
    TokenAcesso entrar(String idToken);
}
