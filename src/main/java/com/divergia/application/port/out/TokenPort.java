package com.divergia.application.port.out;

import com.divergia.domain.model.TokenAcesso;

import java.util.UUID;

public interface TokenPort {

    TokenAcesso gerar(UUID usuarioId);

    TokenAcesso validar(String tokenBruto);
}
