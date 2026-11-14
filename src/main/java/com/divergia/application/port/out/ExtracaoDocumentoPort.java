package com.divergia.application.port.out;

public interface ExtracaoDocumentoPort {
    String extrairTexto(byte[] conteudo, String nomeArquivo);
}
