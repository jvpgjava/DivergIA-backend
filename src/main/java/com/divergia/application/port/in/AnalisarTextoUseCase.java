package com.divergia.application.port.in;

import com.divergia.domain.model.ResultadoAnalise;

public interface AnalisarTextoUseCase {
    ResultadoAnalise analisar(EntradaAnalise entrada);
}
