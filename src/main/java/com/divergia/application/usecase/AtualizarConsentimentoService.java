package com.divergia.application.usecase;

import com.divergia.application.port.in.AtualizarConsentimentoUseCase;
import com.divergia.application.port.out.ConsentimentoRepositoryPort;
import com.divergia.domain.model.Consentimento;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AtualizarConsentimentoService implements AtualizarConsentimentoUseCase {

    private final ConsentimentoRepositoryPort consentimentoRepository;

    public AtualizarConsentimentoService(ConsentimentoRepositoryPort consentimentoRepository) {
        this.consentimentoRepository = consentimentoRepository;
    }

    @Override
    public Consentimento atualizar(UUID usuarioId, boolean manterHistorico, boolean contribuirParaRag) {
        Consentimento novoConsentimento = new Consentimento(
                UUID.randomUUID(), usuarioId, manterHistorico, contribuirParaRag, Instant.now());
        return consentimentoRepository.salvar(novoConsentimento);
    }
}
