package com.divergia.adapter.out.persistence;

import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.domain.model.TrechoDeriva;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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
    public Optional<TrechoDeriva> buscarPorId(UUID id) {
        return repository.findById(id).map(TrechoDerivaMapper::toDomain);
    }

    @Override
    public List<TrechoDeriva> buscarPorAnaliseId(UUID analiseId) {
        return repository.findByAnaliseId(analiseId).stream()
                .map(TrechoDerivaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrechoDeriva> buscarPorUsuarioId(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(TrechoDerivaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrechoDeriva> buscarNaoPromovidosParaRag() {
        return repository.findByPromovidoParaRagFalse().stream()
                .map(TrechoDerivaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void marcarComoPromovidoParaRag(UUID id) {
        TrechoDerivaJpaEntity existente = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Trecho de deriva não encontrado: " + id));
        TrechoDerivaJpaEntity atualizado = new TrechoDerivaJpaEntity(
                existente.getId(),
                existente.getAnaliseId(),
                existente.getTrechoOriginal(),
                existente.getTrechoEditado(),
                existente.getTipoDesvio(),
                existente.getExplicacao(),
                existente.getIntensidade(),
                true,
                existente.getSugestaoAceita());
        repository.save(atualizado);
    }
}
