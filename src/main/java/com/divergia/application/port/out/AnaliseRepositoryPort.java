package com.divergia.application.port.out;

import com.divergia.domain.model.Analise;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnaliseRepositoryPort {

    Analise salvar(Analise analise);

    Optional<Analise> buscarPorId(UUID id);

    List<Analise> buscarPorUsuarioId(UUID usuarioId);

    void excluir(UUID id);

    void excluirTodasPorUsuarioId(UUID usuarioId);
}
