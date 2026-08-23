package com.divergia.application.port.out;

public interface ExtracaoDocumentoPort {

    /**
     * @param conteudo bytes brutos do arquivo enviado pelo usuário
     * @param nomeArquivo nome original do arquivo (usado para o serviço identificar o formato)
     * @return texto extraído do documento
     * @throws ExtracaoDocumentoException se o serviço de extração estiver indisponível,
     *         responder fora do tempo limite, ou não conseguir processar o documento
     */
    String extrairTexto(byte[] conteudo, String nomeArquivo);
}
