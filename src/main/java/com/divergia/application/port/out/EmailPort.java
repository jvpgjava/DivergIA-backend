package com.divergia.application.port.out;

public interface EmailPort {

    void enviarRecuperacaoSenha(String destinatario, String tokenBruto);
}
