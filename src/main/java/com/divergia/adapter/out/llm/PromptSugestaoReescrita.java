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
                .append("do texto.\n\n");

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
                .append("Gere EXATAMENTE 3 alternativas de reescrita, cada uma fiel ao sentido, posição e ")
                .append("intensidade do trecho original, mas com fraseio genuinamente distinto entre si — ")
                .append("três formas diferentes de dizer a mesma coisa fielmente, não variações triviais de ")
                .append("pontuação ou sinônimos isolados. NENHUMA das 3 alternativas pode ser apenas o texto ")
                .append("original repetido palavra por palavra — reescreva de fato, só preservando o sentido, ")
                .append("a posição e a intensidade originais.\n\n")
                .append("Responda EXCLUSIVAMENTE com um array JSON de 3 strings, sem markdown, sem explicação ")
                .append("antes ou depois. Exemplo do formato exato esperado:\n")
                .append("[\"primeira alternativa\", \"segunda alternativa\", \"terceira alternativa\"]");

        return prompt.toString();
    }
}
