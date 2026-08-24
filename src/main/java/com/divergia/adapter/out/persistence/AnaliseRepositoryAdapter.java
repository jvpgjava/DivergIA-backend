package com.divergia.adapter.out.persistence;

import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.domain.model.Analise;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AnaliseRepositoryAdapter implements AnaliseRepositoryPort {

    private final AnaliseJpaRepository repository;

    public AnaliseRepositoryAdapter(AnaliseJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Analise salvar(Analise analise) {
        return AnaliseMapper.toDomain(repository.save(AnaliseMapper.toEntity(analise)));
    }

    @Override
    public Optional<Analise> buscarPorId(UUID id) {
        return repository.findById(id).map(AnaliseMapper::toDomain);
    }

    @Override
    public List<Analise> buscarPorUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(AnaliseMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void excluir(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public void excluirTodasPorUsuarioId(UUID usuarioId) {
        repository.deleteByUsuarioId(usuarioId);
    }
}
