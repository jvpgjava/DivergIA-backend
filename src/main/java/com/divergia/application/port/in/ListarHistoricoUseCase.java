package com.divergia.application.port.in;

import com.divergia.domain.model.Analise;

import java.util.List;
import java.util.UUID;

public interface ListarHistoricoUseCase {

    List<Analise> listar(UUID usuarioId);
}
