package com.divergia.application.usecase;

import com.divergia.application.port.in.BuscarAnaliseUseCase;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.ResultadoAnalise;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BuscarAnaliseService implements BuscarAnaliseUseCase {

    private final AnaliseRepositoryPort analiseRepository;
    private final TrechoDerivaRepositoryPort trechoDerivaRepository;

    public BuscarAnaliseService(
            AnaliseRepositoryPort analiseRepository, TrechoDerivaRepositoryPort trechoDerivaRepository) {
        this.analiseRepository = analiseRepository;
        this.trechoDerivaRepository = trechoDerivaRepository;
    }

    @Override
    public ResultadoAnalise buscar(UUID usuarioId, UUID analiseId) {
        Analise analise = analiseRepository.buscarPorId(analiseId)
                .orElseThrow(AnaliseNaoEncontradaException::new);

        if (!analise.usuarioId().equals(usuarioId)) {
            throw new AcessoNaoAutorizadoException();
        }

        return new ResultadoAnalise(analise, trechoDerivaRepository.buscarPorAnaliseId(analiseId));
    }
}
