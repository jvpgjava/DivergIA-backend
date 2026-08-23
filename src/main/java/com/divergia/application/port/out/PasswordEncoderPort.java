package com.divergia.application.port.out;

public interface PasswordEncoderPort {

    String codificar(String senhaEmTextoPlano);

    boolean confere(String senhaEmTextoPlano, String senhaHash);
}
