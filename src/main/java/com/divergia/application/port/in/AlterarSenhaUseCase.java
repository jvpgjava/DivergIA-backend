package com.divergia.application.port.in;

import java.util.UUID;

public interface AlterarSenhaUseCase {
    void alterar(UUID usuarioId, String senhaAtual, String novaSenha);
}
