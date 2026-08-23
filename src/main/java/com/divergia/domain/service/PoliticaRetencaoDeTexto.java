package com.divergia.domain.service;

import com.divergia.domain.model.Analise;

/**
 * Regra de domínio: o texto bruto de uma análise (original e editado) não é
 * mantido além do necessário para o processamento, a menos que o usuário
 * tenha consentido explicitamente em manter histórico. Esta é a única forma
 * autorizada de decidir se o texto de uma {@link Analise} é retido — nenhuma
 * outra camada deve reimplementar essa decisão.
 */
public class PoliticaRetencaoDeTexto {

    /**
     * Aplica a regra de retenção sobre uma análise recém-produzida (com o
     * texto bruto ainda presente). Se {@code analise.manterHistorico()} for
     * falso, retorna uma cópia sem o texto bruto; caso contrário, retorna a
     * análise inalterada.
     */
    public Analise aplicar(Analise analise) {
        if (analise.manterHistorico()) {
            return analise;
        }
        return new Analise(
                analise.id(),
                analise.usuarioId(),
                null,
                null,
                false,
                analise.criadoEm());
    }
}
