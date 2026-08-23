package com.divergia.application.port.out;

import java.time.Instant;

public interface TokenRevogadoPort {

    void revogar(String jti, Instant expiraEm);

    boolean estaRevogado(String jti);
}
