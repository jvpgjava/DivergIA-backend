package com.divergia.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleLoginRequest(
        @NotBlank(message = "idToken é obrigatório")
        @Size(max = 4096, message = "idToken inválido") String idToken) {
}
