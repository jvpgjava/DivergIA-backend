package com.divergia.application.port.out;

import com.divergia.domain.model.TrechoDeriva;

import java.util.List;
import java.util.UUID;

public interface TrechoDerivaRepositoryPort {

    TrechoDeriva salvar(TrechoDeriva trecho);

    List<TrechoDeriva> buscarPorAnaliseId(UUID analiseId);
}
