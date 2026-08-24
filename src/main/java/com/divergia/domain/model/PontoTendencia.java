package com.divergia.domain.model;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Agregado de um mês no painel de tendência pessoal: quantas análises e
 * derivas o usuário teve, e a intensidade média das derivas naquele mês.
 */
public record PontoTendencia(YearMonth mes, long quantidadeAnalises, long quantidadeDerivas, double intensidadeMedia) {

    public PontoTendencia {
        Objects.requireNonNull(mes, "mes não pode ser nulo");
        if (quantidadeAnalises < 0 || quantidadeDerivas < 0) {
            throw new IllegalArgumentException("quantidades não podem ser negativas");
        }
    }
}
