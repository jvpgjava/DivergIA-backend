package com.divergia.application.port.out;

public class ExtracaoDocumentoException extends RuntimeException {

    public ExtracaoDocumentoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtracaoDocumentoException(String message) {
        super(message);
    }
}
