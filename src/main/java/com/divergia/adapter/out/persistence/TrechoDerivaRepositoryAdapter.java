package com.divergia.adapter.out.persistence;

import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.domain.model.TrechoDeriva;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TrechoDerivaRepositoryAdapter implements TrechoDerivaRepositoryPort {

    private final TrechoDerivaJpaRepository repository;

    public TrechoDerivaRepositoryAdapter(TrechoDerivaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public TrechoDeriva salvar(TrechoDeriva trecho) {
        return TrechoDerivaMapper.toDomain(repository.save(TrechoDerivaMapper.toEntity(trecho)));
    }

    @Override
    public List<TrechoDeriva> buscarPorAnaliseId(UUID analiseId) {
        return repository.findByAnaliseId(analiseId).stream()
                .map(TrechoDerivaMapper::toDomain)
                .collect(Collectors.toList());
    }
}
