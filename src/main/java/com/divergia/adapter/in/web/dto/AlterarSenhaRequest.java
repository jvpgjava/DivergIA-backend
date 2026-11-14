package com.divergia.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequest(
        @NotBlank(message = "senhaAtual é obrigatória")
        @Size(max = Limites.SENHA_INFORMADA, message = "senha muito longa") String senhaAtual,
        @NotBlank(message = "novaSenha é obrigatória")
        @Size(min = Limites.SENHA_MINIMO, max = Limites.SENHA_MAXIMO, message = "novaSenha deve ter entre 8 e 64 caracteres")
                String novaSenha) {
}
