package com.divergia.application.port.in;

import java.util.UUID;

public interface ExcluirAnaliseUseCase {
    void excluir(UUID usuarioId, UUID analiseId);
}
