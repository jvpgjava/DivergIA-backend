package com.divergia.application.port.in;

public interface EncerrarSessaoUseCase {

    /**
     * Revoga o token de acesso apresentado, para que não possa mais ser usado
     * mesmo antes de sua expiração natural.
     */
    void encerrar(String tokenBruto);
}
