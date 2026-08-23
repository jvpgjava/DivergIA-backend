package com.divergia.adapter.out.security;

import com.divergia.application.port.out.PasswordEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private static final int CUSTO_BCRYPT = 12;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(CUSTO_BCRYPT);

    @Override
    public String codificar(String senhaEmTextoPlano) {
        return encoder.encode(senhaEmTextoPlano);
    }

    @Override
    public boolean confere(String senhaEmTextoPlano, String senhaHash) {
        return encoder.matches(senhaEmTextoPlano, senhaHash);
    }
}
