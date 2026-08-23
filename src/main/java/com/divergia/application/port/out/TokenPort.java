package com.divergia.application.port.out;

import com.divergia.domain.model.TokenAcesso;

import java.util.UUID;

public interface TokenPort {

    TokenAcesso gerar(UUID usuarioId);

    /**
     * @throws IllegalArgumentException se o token for inválido, malformado ou estiver expirado
     */
    TokenAcesso validar(String tokenBruto);
}
