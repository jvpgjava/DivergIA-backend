package com.divergia.application.usecase;

import com.divergia.application.port.in.ExcluirHistoricoUseCase;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExcluirHistoricoService implements ExcluirHistoricoUseCase {

    private final AnaliseRepositoryPort analiseRepository;

    public ExcluirHistoricoService(AnaliseRepositoryPort analiseRepository) {
        this.analiseRepository = analiseRepository;
    }

    @Override
    public void excluirTudo(UUID usuarioId) {
        analiseRepository.excluirTodasPorUsuarioId(usuarioId);
    }
}
