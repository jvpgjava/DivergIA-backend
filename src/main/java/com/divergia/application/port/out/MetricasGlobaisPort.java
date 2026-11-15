package com.divergia.application.port.out;

import com.divergia.domain.model.EstatisticasGlobais;
import com.divergia.domain.model.TipoDesvio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface MetricasGlobaisPort {

    void registrarAnalise(Instant momento, List<TipoDesvio> derivas);

    EstatisticasGlobais consultar(LocalDate serieDesde, Instant geradoEm);
}
