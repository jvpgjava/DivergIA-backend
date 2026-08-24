package com.divergia.application.port.out;

import com.divergia.domain.model.TrechoDeriva;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrechoDerivaRepositoryPort {

    TrechoDeriva salvar(TrechoDeriva trecho);

    Optional<TrechoDeriva> buscarPorId(UUID id);

    List<TrechoDeriva> buscarPorAnaliseId(UUID analiseId);
}
