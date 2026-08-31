package com.divergia.adapter.out.llm;

import com.divergia.application.port.out.LlmException;
import com.divergia.application.port.out.LlmPort;
import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.TipoDesvio;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AbacusLlmAdapter implements LlmPort {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AbacusLlmAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public List<AvaliacaoDeDeriva> avaliarDerivas(
            String textoOriginal, String textoEditado, List<ExemploRag> exemplosRelevantes) {
        String prompt = PromptAvaliacaoDeriva.montar(textoOriginal, textoEditado, exemplosRelevantes);
        String resposta = chatModel.chat(prompt);
        return parsear(resposta);
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
        String resposta = chatModel.chat(prompt);
        return parsearSugestoes(resposta);
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
            resultado.add(new AvaliacaoDeDeriva(
                    TipoDesvio.valueOf(deriva.tipoDesvio().toUpperCase()),
                    deriva.trechoOriginal(),
                    deriva.trechoEditado(),
                    deriva.explicacao(),
                    deriva.intensidade()));
        }
        return resultado;
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
