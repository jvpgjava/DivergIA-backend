package com.divergia.application.port.in;

public interface SolicitarRecuperacaoSenhaUseCase {

    /**
     * Não revela se o e-mail existe ou não na base — sempre se comporta da
     * mesma forma do ponto de vista do chamador, para evitar enumeração de
     * usuários.
     */
    void solicitar(String email);
}
