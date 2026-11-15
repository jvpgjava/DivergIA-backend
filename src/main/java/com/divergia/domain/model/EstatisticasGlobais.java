package com.divergia.domain.model;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EstatisticasGlobais(
        long totalAnalises,
        long totalDerivas,
        Map<TipoDesvio, Long> derivasPorTipo,
        List<PontoDiarioGlobal> serieDiaria,
        Instant geradoEm) {

    public EstatisticasGlobais {
        Objects.requireNonNull(derivasPorTipo, "derivasPorTipo não pode ser nulo");
        Objects.requireNonNull(serieDiaria, "serieDiaria não pode ser nula");
        Objects.requireNonNull(geradoEm, "geradoEm não pode ser nulo");
        EnumMap<TipoDesvio, Long> completo = new EnumMap<>(TipoDesvio.class);
        for (TipoDesvio tipo : TipoDesvio.values()) {
            completo.put(tipo, derivasPorTipo.getOrDefault(tipo, 0L));
        }
        derivasPorTipo = Map.copyOf(completo);
        serieDiaria = List.copyOf(serieDiaria);
    }
}
