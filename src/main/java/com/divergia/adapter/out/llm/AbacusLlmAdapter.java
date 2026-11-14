package com.divergia.adapter.out.llm;

import com.divergia.application.port.out.LlmException;
import com.divergia.application.port.out.LlmPort;
import com.divergia.domain.model.AvaliacaoDoModelo;
import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.TipoDesvio;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class AbacusLlmAdapter implements LlmPort {

    private static final Logger log = LoggerFactory.getLogger(AbacusLlmAdapter.class);

    static final int MAX_TOKENS_ANALISE = 4096;
    static final int MAX_TOKENS_REESCRITA = 1024;
    static final int MAX_TOKENS_JUIZ = 400;

    private final ChatModel chatModel;
    private final LlmProperties properties;
    private final PromptsIa prompts;

    public AbacusLlmAdapter(ChatModel chatModel, LlmProperties properties) {
        this.chatModel = chatModel;
        this.properties = properties;
        this.prompts = PromptsIa.carregar();
    }

    @Override
    public AvaliacaoDoModelo avaliarDerivas(
            String textoOriginal, String textoEditado, List<ExemploRag> exemplosRelevantes) {
        RespostaDoModelo.Analise analise = chamarEstruturado(
                "análise",
                prompts.analise(),
                MensagensIa.analise(textoOriginal, textoEditado, exemplosRelevantes),
                properties.temperaturaAnalise(),
                MAX_TOKENS_ANALISE,
                RespostaDoModelo::analise,
                a -> a.itensMalformados() == 0 ? null
                        : a.itensMalformados() + " item(ns) de deriva sem todos os campos obrigatórios");
        if (analise.itensMalformados() > 0) {
            log.warn("Análise: {} item(ns) de deriva malformado(s) ignorado(s)", analise.itensMalformados());
        }
        return analise.avaliacao();
    }

    @Override
    public List<String> sugerirReescrita(
            String trechoOriginal,
            String trechoEditado,
            TipoDesvio tipoDesvio,
            String explicacao,
            List<ExemploRag> exemplosRelevantes) {
        return chamarEstruturado(
                "reescrita",
                prompts.reescrita(),
                MensagensIa.reescrita(trechoOriginal, trechoEditado, tipoDesvio, explicacao, exemplosRelevantes),
                properties.temperaturaReescrita(),
                MAX_TOKENS_REESCRITA,
                RespostaDoModelo::sugestoes,
                completa -> null);
    }

    @Override
    public List<Boolean> verificarFidelidade(String trechoOriginal, List<String> sugestoes) {
        if (sugestoes == null || sugestoes.isEmpty()) {
            return List.of();
        }
        try {
            return chamarEstruturado(
                    "juiz de fidelidade",
                    prompts.juizFidelidade(),
                    MensagensIa.juiz(trechoOriginal, sugestoes),
                    properties.temperaturaAnalise(),
                    MAX_TOKENS_JUIZ,
                    resposta -> RespostaDoModelo.vereditos(resposta, sugestoes.size()),
                    completa -> null);
        } catch (RuntimeException e) {
            // O juiz nunca lança: lista vazia sinaliza indisponibilidade a quem chama.
            log.warn("Juiz de fidelidade indisponível ({}); seguindo sem ele", e.getClass().getSimpleName());
            return List.of();
        }
    }

    // Uma única tentativa de reparo, tanto para JSON quebrado quanto para resposta incompleta;
    // se o reparo não resolver, vale a primeira resposta utilizável (nunca uma pior).
    private <T> T chamarEstruturado(
            String tarefa, String sistema, String usuario, Double temperatura, int maxTokens,
            Function<String, T> interpretar, Function<T, String> incompleta) {
        List<ChatMessage> mensagens = List.of(SystemMessage.from(sistema), UserMessage.from(usuario));
        String resposta = chamar(mensagens, temperatura, maxTokens);
        T primeiro = null;
        String problema;
        try {
            primeiro = interpretar.apply(resposta);
            problema = incompleta.apply(primeiro);
            if (problema == null) {
                return primeiro;
            }
        } catch (RespostaDoModelo.ForaDoFormato e) {
            problema = e.getMessage();
        }

        log.warn("{}: resposta fora do formato ({}); tentando reparo", tarefa, problema);
        List<ChatMessage> reparo = List.of(
                SystemMessage.from(sistema),
                UserMessage.from(usuario),
                AiMessage.from(resposta == null || resposta.isBlank() ? "(resposta vazia)" : resposta),
                UserMessage.from(MensagensIa.reparo(problema)));
        String segunda = chamar(reparo, temperatura, maxTokens);
        try {
            T reparado = interpretar.apply(segunda);
            if (primeiro == null || incompleta.apply(reparado) == null) {
                return reparado;
            }
        } catch (RespostaDoModelo.ForaDoFormato e) {
            if (primeiro == null) {
                throw new LlmException(
                        "Resposta do modelo fora do formato na " + tarefa + " mesmo após reparo: " + e.getMessage());
            }
        }
        return primeiro;
    }

    private String chamar(List<ChatMessage> mensagens, Double temperatura, int maxTokens) {
        if (!properties.temChave()) {
            throw new LlmException("ABACUS_API_KEY não configurada (defina no arquivo .env)");
        }
        ChatRequest.Builder requisicao = ChatRequest.builder().messages(mensagens).maxOutputTokens(maxTokens);
        if (Boolean.TRUE.equals(properties.enviarTemperatura()) && temperatura != null) {
            requisicao.temperature(temperatura);
        }
        ChatResponse resposta;
        try {
            resposta = chatModel.chat(requisicao.build());
        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmException("Falha ao chamar o modelo de linguagem: " + resumo(e), e);
        }
        if (resposta == null || resposta.aiMessage() == null) {
            throw new LlmException("Modelo de linguagem devolveu resposta vazia");
        }
        return resposta.aiMessage().text();
    }

    private static String resumo(Exception e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().replaceAll("\\s+", " ");
        return e.getClass().getSimpleName() + (msg.isEmpty() ? "" : ": " + (msg.length() > 200 ? msg.substring(0, 200) + "…" : msg));
    }
}
