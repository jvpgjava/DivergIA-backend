package com.divergia.application.port.in;

import java.util.List;
import java.util.UUID;

public interface SugerirReescritaUseCase {
    List<String> sugerir(UUID usuarioId, UUID trechoDerivaId);
}
