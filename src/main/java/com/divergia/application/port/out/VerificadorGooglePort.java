package com.divergia.application.port.out;

import com.divergia.domain.model.IdentidadeGoogle;

import java.util.Optional;

public interface VerificadorGooglePort {

    // Vazio quando o token não é válido (assinatura, emissor, destinatário, validade ou e-mail não verificado).
    Optional<IdentidadeGoogle> verificar(String idToken);

    boolean configurado();
}
