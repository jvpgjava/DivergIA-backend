package com.divergia.adapter.out.llm;

import com.divergia.application.port.out.LlmException;
import com.divergia.application.port.out.LlmPort;
import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.TipoDesvio;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AbacusLlmAdapter implements LlmPort {

    private static final Logger log = LoggerFactory.getLogger(AbacusLlmAdapter.class);

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AbacusLlmAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public List<AvaliacaoDeDeriva> avaliarDerivas(
            String textoOriginal, String textoEditado, List<ExemploRag> exemplosRelevantes) {
        String prompt = PromptAvaliacaoDeriva.montar(textoOriginal, textoEditado, exemplosRelevantes);
        return parsear(chamar(prompt));
    }

    @Override
    public List<String> sugerirReescrita(
            String trechoOriginal,
            String trechoEditado,
            TipoDesvio tipoDesvio,
            String explicacao,
            List<ExemploRag> exemplosRelevantes) {
        String prompt = PromptSugestaoReescrita.montar(
                trechoOriginal, trechoEditado, tipoDesvio, explicacao, exemplosRelevantes);
        return parsearSugestoes(chamar(prompt));
    }

    private String chamar(String prompt) {
        try {
            return chatModel.chat(prompt);
        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmException("Falha ao chamar o modelo de linguagem: " + e.getMessage(), e);
        }
    }

    private List<String> parsearSugestoes(String resposta) {
        String json = extrairArrayJson(resposta);
        try {
            return objectMapper.readValue(
                    json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            throw new LlmException(
                    "Resposta do LLM não pôde ser interpretada como JSON de sugestões (tamanho: "
                            + resposta.length() + " caracteres)", e);
        }
    }

    private List<AvaliacaoDeDeriva> parsear(String resposta) {
        String json = extrairArrayJson(resposta);
        List<DerivaJson> derivas;
        try {
            derivas = objectMapper.readValue(
                    json, objectMapper.getTypeFactory().constructCollectionType(List.class, DerivaJson.class));
        } catch (Exception e) {
            throw new LlmException(
                    "Resposta do LLM não pôde ser interpretada como JSON de derivas (tamanho: "
                            + resposta.length() + " caracteres)", e);
        }

        List<AvaliacaoDeDeriva> resultado = new ArrayList<>();
        for (DerivaJson deriva : derivas) {
            try {
                resultado.add(new AvaliacaoDeDeriva(
                        TipoDesvio.valueOf(deriva.tipoDesvio().toUpperCase()),
                        deriva.trechoOriginal(),
                        deriva.trechoEditado(),
                        deriva.explicacao(),
                        clampIntensidade(deriva.intensidade())));
            } catch (Exception e) {
                // Um item malformado (ex: LLM devolveu intensidade fora de 0-1, um
                // tipoDesvio inexistente, ou trecho vazio — comum quando o texto de
                // entrada é degenerado, como só números ou dígitos binários) não pode
                // derrubar a análise inteira; os demais itens válidos ainda são úteis.
                log.warn("Ignorando item de deriva malformado na resposta do LLM: {}", e.getMessage());
            }
        }
        return resultado;
    }

    /**
     * Alguns modelos respondem a intensidade em escala 0-100 (ex.: 90) em vez
     * de 0.0-1.0 mesmo quando instruídos — comum com entradas degeneradas
     * (números, binário). Reescala em vez de descartar o item inteiro.
     */
    private double clampIntensidade(double intensidade) {
        double normalizada = intensidade > 1.0 ? intensidade / 100.0 : intensidade;
        return Math.max(0.0, Math.min(1.0, normalizada));
    }

    private String extrairArrayJson(String resposta) {
        int inicio = resposta.indexOf('[');
        int fim = resposta.lastIndexOf(']');
        if (inicio == -1 || fim == -1 || fim < inicio) {
            throw new LlmException(
                    "Resposta do LLM não contém um array JSON (tamanho: " + resposta.length() + " caracteres)");
        }
        return resposta.substring(inicio, fim + 1);
    }

    private record DerivaJson(
            String tipoDesvio, String trechoOriginal, String trechoEditado, String explicacao, double intensidade) {
    }
}
