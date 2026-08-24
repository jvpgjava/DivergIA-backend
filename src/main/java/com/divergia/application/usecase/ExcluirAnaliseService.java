package com.divergia.application.usecase;

import com.divergia.application.port.in.ExcluirAnaliseUseCase;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.domain.model.Analise;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExcluirAnaliseService implements ExcluirAnaliseUseCase {

    private final AnaliseRepositoryPort analiseRepository;

    public ExcluirAnaliseService(AnaliseRepositoryPort analiseRepository) {
        this.analiseRepository = analiseRepository;
    }

    @Override
    public void excluir(UUID usuarioId, UUID analiseId) {
        Analise analise = analiseRepository.buscarPorId(analiseId)
                .orElseThrow(AnaliseNaoEncontradaException::new);

        if (!analise.usuarioId().equals(usuarioId)) {
            throw new AcessoNaoAutorizadoException();
        }

        analiseRepository.excluir(analiseId);
    }
}
