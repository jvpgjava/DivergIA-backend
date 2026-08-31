package com.divergia.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AlterarEmailRequest(
        @NotBlank(message = "novoEmail é obrigatório") @Email(message = "email inválido") String novoEmail,
        @NotBlank(message = "senhaAtual é obrigatória") String senhaAtual) {
}
