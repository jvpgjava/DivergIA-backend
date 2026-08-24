package com.divergia.adapter.out.llm;

import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.TipoDesvio;

import java.util.List;

final class PromptSugestaoReescrita {

    private PromptSugestaoReescrita() {
    }

    static String montar(
            String trechoOriginal, String trechoEditado, TipoDesvio tipoDesvio, String explicacao,
            List<ExemploRag> exemplos) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Você é um editor que corrige um trecho editado por IA generativa para que volte a ser ")
                .append("fiel ao sentido, posição e intensidade do trecho original, mantendo a fluidez natural ")
                .append("do texto (a reescrita não precisa ser idêntica ao original, só fiel a ele).\n\n");

        if (!exemplos.isEmpty()) {
            prompt.append("Exemplos de referência de derivas semelhantes:\n");
            for (ExemploRag exemplo : exemplos) {
                prompt.append("- Original: \"").append(exemplo.textoOriginal()).append("\"\n")
                        .append("  Editado: \"").append(exemplo.textoEditado()).append("\"\n");
            }
            prompt.append('\n');
        }

        prompt.append("Trecho original:\n").append(trechoOriginal).append("\n\n")
                .append("Trecho editado (com problema):\n").append(trechoEditado).append("\n\n")
                .append("Tipo de desvio identificado: ").append(tipoDesvio).append("\n")
                .append("Por que foi considerado um desvio: ").append(explicacao).append("\n\n")
                .append("Responda EXCLUSIVAMENTE com o texto da reescrita sugerida — sem aspas, ")
                .append("sem markdown, sem explicação antes ou depois.");

        return prompt.toString();
    }
}
