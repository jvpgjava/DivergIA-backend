package com.divergia.application.usecase;

import com.divergia.application.port.in.ListarHistoricoUseCase;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.domain.model.Analise;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ListarHistoricoService implements ListarHistoricoUseCase {

    private final AnaliseRepositoryPort analiseRepository;

    public ListarHistoricoService(AnaliseRepositoryPort analiseRepository) {
        this.analiseRepository = analiseRepository;
    }

    @Override
    public List<Analise> listar(UUID usuarioId) {
        return analiseRepository.buscarPorUsuarioId(usuarioId).stream()
                .sorted(Comparator.comparing(Analise::criadoEm).reversed())
                .toList();
    }
}
