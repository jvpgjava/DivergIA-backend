package com.divergia.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank(message = "token é obrigatório")
        @Size(max = Limites.TOKEN_RECUPERACAO, message = "token inválido") String token,
        @NotBlank(message = "novaSenha é obrigatória")
        @Size(min = Limites.SENHA_MINIMO, max = Limites.SENHA_MAXIMO, message = "novaSenha deve ter entre 8 e 64 caracteres")
                String novaSenha) {
}
