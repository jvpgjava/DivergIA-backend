package com.divergia.application.port.out;

// A mensagem nunca leva texto do usuário nem resposta crua do modelo: ela vai para o log.
public class LlmException extends RuntimeException {

    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
