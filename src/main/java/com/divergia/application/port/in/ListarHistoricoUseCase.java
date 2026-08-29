package com.divergia.application.port.in;

import com.divergia.domain.model.ResultadoAnalise;

import java.util.List;
import java.util.UUID;

public interface ListarHistoricoUseCase {

    List<ResultadoAnalise> listar(UUID usuarioId);
}
