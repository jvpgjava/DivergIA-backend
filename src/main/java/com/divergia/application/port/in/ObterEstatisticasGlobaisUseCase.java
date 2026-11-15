package com.divergia.application.port.in;

import com.divergia.domain.model.EstatisticasGlobais;

import java.util.UUID;

public interface ObterEstatisticasGlobaisUseCase {

    EstatisticasGlobais obter(UUID solicitanteId, int dias);
}
