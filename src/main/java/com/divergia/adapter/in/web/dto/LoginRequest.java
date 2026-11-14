package com.divergia.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "email é obrigatório")
        @Size(max = Limites.EMAIL, message = "email muito longo")
        @Email(message = "email inválido") String email,
        @NotBlank(message = "senha é obrigatória")
        @Size(max = Limites.SENHA_INFORMADA, message = "senha muito longa") String senha) {
}
