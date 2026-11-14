package com.divergia.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AceitarSugestaoRequest(
        @NotBlank(message = "texto é obrigatório")
        @Size(max = Limites.SUGESTAO, message = "texto muito longo") String texto) {
}
