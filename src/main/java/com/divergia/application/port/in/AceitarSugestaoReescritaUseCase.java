package com.divergia.application.port.in;

import java.util.UUID;

public interface AceitarSugestaoReescritaUseCase {
    void aceitar(UUID usuarioId, UUID trechoDerivaId, String textoEscolhido);
}
