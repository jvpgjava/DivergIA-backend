package com.divergia.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarEmailRequest(
        @NotBlank(message = "novoEmail é obrigatório")
        @Size(max = Limites.EMAIL, message = "email muito longo")
        @Email(message = "email inválido") String novoEmail,
        @NotBlank(message = "senhaAtual é obrigatória")
        @Size(max = Limites.SENHA_INFORMADA, message = "senha muito longa") String senhaAtual) {
}
