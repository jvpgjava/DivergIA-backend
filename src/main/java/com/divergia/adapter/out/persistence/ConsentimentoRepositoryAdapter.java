package com.divergia.adapter.out.persistence;

import com.divergia.application.port.out.ConsentimentoRepositoryPort;
import com.divergia.domain.model.Consentimento;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ConsentimentoRepositoryAdapter implements ConsentimentoRepositoryPort {

    private final ConsentimentoJpaRepository repository;

    public ConsentimentoRepositoryAdapter(ConsentimentoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Consentimento salvar(Consentimento consentimento) {
        return ConsentimentoMapper.toDomain(repository.save(ConsentimentoMapper.toEntity(consentimento)));
    }

    @Override
    public Optional<Consentimento> buscarMaisRecentePorUsuarioId(UUID usuarioId) {
        return repository.findFirstByUsuarioIdOrderByConcedidoEmDesc(usuarioId).map(ConsentimentoMapper::toDomain);
    }
}
