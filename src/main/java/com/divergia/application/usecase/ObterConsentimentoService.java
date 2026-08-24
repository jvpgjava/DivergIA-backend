package com.divergia.application.usecase;

import com.divergia.application.port.in.ObterConsentimentoUseCase;
import com.divergia.application.port.out.ConsentimentoRepositoryPort;
import com.divergia.domain.model.Consentimento;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ObterConsentimentoService implements ObterConsentimentoUseCase {

    private final ConsentimentoRepositoryPort consentimentoRepository;

    public ObterConsentimentoService(ConsentimentoRepositoryPort consentimentoRepository) {
        this.consentimentoRepository = consentimentoRepository;
    }

    @Override
    public Consentimento obter(UUID usuarioId) {
        return consentimentoRepository.buscarMaisRecentePorUsuarioId(usuarioId)
                .orElseGet(() -> new Consentimento(UUID.randomUUID(), usuarioId, false, false, Instant.now()));
    }
}
