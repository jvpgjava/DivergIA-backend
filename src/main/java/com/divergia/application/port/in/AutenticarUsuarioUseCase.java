package com.divergia.application.port.in;

import com.divergia.domain.model.TokenAcesso;

public interface AutenticarUsuarioUseCase {
    TokenAcesso autenticar(String email, String senha);
}
