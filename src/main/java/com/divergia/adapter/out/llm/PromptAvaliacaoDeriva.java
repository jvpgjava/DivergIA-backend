package com.divergia.adapter.out.llm;

import com.divergia.domain.model.ExemploRag;

import java.util.List;

final class PromptAvaliacaoDeriva {

    private PromptAvaliacaoDeriva() {
    }

    static String montar(String textoOriginal, String textoEditado, List<ExemploRag> exemplos) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Você é um analista que compara um texto original com uma versão editada por ")
                .append("inteligência artificial generativa. Identifique trechos onde a edição alterou o ")
                .append("SENTIDO, a POSIÇÃO ou a INTENSIDADE da mensagem além do que uma correção de estilo ")
                .append("justificaria.\n\n");

        if (!exemplos.isEmpty()) {
            prompt.append("Exemplos de referência de derivas já identificadas:\n");
            for (ExemploRag exemplo : exemplos) {
                prompt.append("- Original: \"").append(exemplo.textoOriginal()).append("\"\n")
                        .append("  Editado: \"").append(exemplo.textoEditado()).append("\"\n")
                        .append("  Tipo de desvio: ").append(exemplo.tipoDesvio()).append("\n");
            }
            prompt.append('\n');
        }

        prompt.append("Texto original:\n").append(textoOriginal).append("\n\n")
                .append("Texto editado:\n").append(textoEditado).append("\n\n")
                .append("Responda EXCLUSIVAMENTE com um array JSON (sem markdown, sem texto antes ou depois), ")
                .append("no formato exato:\n")
                .append("[{\"tipoDesvio\":\"SENTIDO|POSICAO|INTENSIDADE\",\"trechoOriginal\":\"...\",")
                .append("\"trechoEditado\":\"...\",\"explicacao\":\"...\",\"intensidade\":0.0}]\n")
                .append("Se não houver nenhuma deriva, responda com um array vazio: []");

        return prompt.toString();
    }
}
