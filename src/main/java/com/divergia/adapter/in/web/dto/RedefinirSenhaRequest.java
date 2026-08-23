package com.divergia.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank(message = "token é obrigatório") String token,
        @NotBlank(message = "novaSenha é obrigatória")
                @Size(min = 8, message = "novaSenha deve ter ao menos 8 caracteres") String novaSenha) {
}
