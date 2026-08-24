package com.divergia.application.port.out;

/**
 * Falha ao obter ou interpretar uma resposta do LLM. A mensagem nunca deve
 * incluir o conteúdo textual da análise nem a resposta bruta do modelo —
 * só metadados (ex.: tamanho da resposta) — para não vazar dado sensível
 * em log de erro.
 */
public class LlmException extends RuntimeException {

    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
