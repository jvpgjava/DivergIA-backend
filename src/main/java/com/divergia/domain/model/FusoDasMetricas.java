package com.divergia.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class FusoDasMetricas {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private FusoDasMetricas() {
    }

    public static LocalDate diaDe(Instant instante) {
        return instante.atZone(FUSO).toLocalDate();
    }
}
