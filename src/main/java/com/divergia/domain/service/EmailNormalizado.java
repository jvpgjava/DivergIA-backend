package com.divergia.domain.service;

import java.util.Locale;

public final class EmailNormalizado {

    private EmailNormalizado() {
    }

    // Uma pessoa = um e-mail, qualquer que seja a caixa ou o jeito de entrar (senha ou Google).
    public static String de(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }
}
