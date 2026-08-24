package com.divergia.application.usecase;

import com.divergia.application.port.in.ObterPainelTendenciaUseCase;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.PainelTendencia;
import com.divergia.domain.model.PontoTendencia;
import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.TrechoDeriva;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PainelTendenciaService implements ObterPainelTendenciaUseCase {

    private final AnaliseRepositoryPort analiseRepository;
    private final TrechoDerivaRepositoryPort trechoDerivaRepository;

    public PainelTendenciaService(
            AnaliseRepositoryPort analiseRepository, TrechoDerivaRepositoryPort trechoDerivaRepository) {
        this.analiseRepository = analiseRepository;
        this.trechoDerivaRepository = trechoDerivaRepository;
    }

    @Override
    public PainelTendencia obter(UUID usuarioId) {
        List<Analise> analises = analiseRepository.buscarPorUsuarioId(usuarioId);
        List<TrechoDeriva> trechos = trechoDerivaRepository.buscarPorUsuarioId(usuarioId);

        Map<UUID, Instant> criadoEmPorAnaliseId = analises.stream()
                .collect(Collectors.toMap(Analise::id, Analise::criadoEm));

        double intensidadeMedia = trechos.stream().mapToDouble(TrechoDeriva::intensidade).average().orElse(0.0);

        Map<TipoDesvio, Long> derivasPorTipo = new EnumMap<>(TipoDesvio.class);
        for (TipoDesvio tipo : TipoDesvio.values()) {
            derivasPorTipo.put(tipo, 0L);
        }
        for (TrechoDeriva trecho : trechos) {
            derivasPorTipo.merge(trecho.tipoDesvio(), 1L, Long::sum);
        }

        Map<YearMonth, Long> analisesPorMes = analises.stream()
                .collect(Collectors.groupingBy(a -> paraMes(a.criadoEm()), Collectors.counting()));

        Map<YearMonth, List<TrechoDeriva>> trechosPorMes = trechos.stream()
                .filter(t -> criadoEmPorAnaliseId.containsKey(t.analiseId()))
                .collect(Collectors.groupingBy(t -> paraMes(criadoEmPorAnaliseId.get(t.analiseId()))));

        TreeSet<YearMonth> meses = new TreeSet<>();
        meses.addAll(analisesPorMes.keySet());
        meses.addAll(trechosPorMes.keySet());

        List<PontoTendencia> evolucaoMensal = meses.stream()
                .map(mes -> {
                    List<TrechoDeriva> trechosDoMes = trechosPorMes.getOrDefault(mes, List.of());
                    double mediaDoMes = trechosDoMes.stream()
                            .mapToDouble(TrechoDeriva::intensidade)
                            .average()
                            .orElse(0.0);
                    return new PontoTendencia(
                            mes, analisesPorMes.getOrDefault(mes, 0L), trechosDoMes.size(), mediaDoMes);
                })
                .toList();

        return new PainelTendencia(analises.size(), trechos.size(), intensidadeMedia, derivasPorTipo, evolucaoMensal);
    }

    private YearMonth paraMes(Instant instant) {
        return YearMonth.from(instant.atZone(ZoneOffset.UTC));
    }
}
