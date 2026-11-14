package com.divergia.adapter.in.web;

public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagemParaUsuario) {
        super(mensagemParaUsuario);
    }
}
