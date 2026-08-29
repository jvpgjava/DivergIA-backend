package com.divergia.application.usecase;

import com.divergia.application.port.in.ListarHistoricoUseCase;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.ResultadoAnalise;
import com.divergia.domain.model.TrechoDeriva;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ListarHistoricoService implements ListarHistoricoUseCase {

    private final AnaliseRepositoryPort analiseRepository;
    private final TrechoDerivaRepositoryPort trechoDerivaRepository;

    public ListarHistoricoService(
            AnaliseRepositoryPort analiseRepository, TrechoDerivaRepositoryPort trechoDerivaRepository) {
        this.analiseRepository = analiseRepository;
        this.trechoDerivaRepository = trechoDerivaRepository;
    }

    @Override
    public List<ResultadoAnalise> listar(UUID usuarioId) {
        List<Analise> analises = analiseRepository.buscarPorUsuarioId(usuarioId).stream()
                .sorted(Comparator.comparing(Analise::criadoEm).reversed())
                .toList();

        Map<UUID, List<TrechoDeriva>> trechosPorAnaliseId = trechoDerivaRepository.buscarPorUsuarioId(usuarioId)
                .stream()
                .collect(Collectors.groupingBy(TrechoDeriva::analiseId));

        return analises.stream()
                .map(analise -> new ResultadoAnalise(
                        analise, trechosPorAnaliseId.getOrDefault(analise.id(), List.of())))
                .toList();
    }
}
