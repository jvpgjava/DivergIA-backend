package com.divergia.domain.model;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record PontoDiarioGlobal(LocalDate dia, long analises, Map<TipoDesvio, Long> derivasPorTipo) {

    public PontoDiarioGlobal {
        Objects.requireNonNull(dia, "dia não pode ser nulo");
        Objects.requireNonNull(derivasPorTipo, "derivasPorTipo não pode ser nulo");
        if (analises < 0) {
            throw new IllegalArgumentException("analises não pode ser negativo");
        }
        EnumMap<TipoDesvio, Long> completo = new EnumMap<>(TipoDesvio.class);
        for (TipoDesvio tipo : TipoDesvio.values()) {
            completo.put(tipo, derivasPorTipo.getOrDefault(tipo, 0L));
        }
        derivasPorTipo = Map.copyOf(completo);
    }

    public static PontoDiarioGlobal vazio(LocalDate dia) {
        return new PontoDiarioGlobal(dia, 0, Map.of());
    }

    public long totalDerivas() {
        return derivasPorTipo.values().stream().mapToLong(Long::longValue).sum();
    }
}
