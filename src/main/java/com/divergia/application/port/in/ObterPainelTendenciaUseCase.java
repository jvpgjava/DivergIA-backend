package com.divergia.application.port.in;

import com.divergia.domain.model.PainelTendencia;

import java.util.UUID;

public interface ObterPainelTendenciaUseCase {

    PainelTendencia obter(UUID usuarioId);
}
