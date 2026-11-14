package com.divergia.adapter.in.web.dto;

final class Limites {

    static final String NOME = "^[\\p{L}\\p{M}\\p{N} .'-]+$";
    static final int EMAIL = 254;
    static final int SENHA_MINIMO = 8;
    // BCrypt usa só os primeiros 72 bytes; 64 caracteres cabem mesmo com acentos.
    static final int SENHA_MAXIMO = 64;
    static final int SENHA_INFORMADA = 128;
    static final int TOKEN_RECUPERACAO = 32;
    static final int SUGESTAO = 2000;

    private Limites() {
    }
}
