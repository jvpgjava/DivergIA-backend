package com.divergia.application.port.out;

/**
 * Falha ao extrair texto de um documento — serviço de extração indisponível,
 * fora do tempo limite, ou o documento em si não pôde ser processado.
 */
public class ExtracaoDocumentoException extends RuntimeException {

    public ExtracaoDocumentoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtracaoDocumentoException(String message) {
        super(message);
    }
}
