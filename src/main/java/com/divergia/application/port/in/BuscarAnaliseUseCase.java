package com.divergia.application.port.in;

import com.divergia.domain.model.ResultadoAnalise;

import java.util.UUID;

public interface BuscarAnaliseUseCase {
    ResultadoAnalise buscar(UUID usuarioId, UUID analiseId);
}
