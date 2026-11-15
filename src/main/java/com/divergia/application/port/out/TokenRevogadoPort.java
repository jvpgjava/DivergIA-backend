package com.divergia.application.port.out;

public interface TokenRevogadoPort {

    void revogar(String jti);

    boolean estaRevogado(String jti);
}
