package com.divergia.application.port.out;

import com.divergia.domain.model.Consentimento;

import java.util.Optional;
import java.util.UUID;

public interface ConsentimentoRepositoryPort {

    Consentimento salvar(Consentimento consentimento);

    Optional<Consentimento> buscarMaisRecentePorUsuarioId(UUID usuarioId);
}
