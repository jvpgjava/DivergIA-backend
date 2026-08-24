package com.divergia.application.port.in;

import com.divergia.domain.model.ResultadoAnalise;

public interface AnalisarTextoUseCase {

    /**
     * Fluxo: validação → extração (se algum lado for arquivo) → busca de
     * exemplos via RAG → avaliação pelo LLM → persistência (respeitando a
     * regra de retenção de dado).
     */
    ResultadoAnalise analisar(EntradaAnalise entrada);
}
