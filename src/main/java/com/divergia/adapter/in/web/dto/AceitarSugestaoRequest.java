package com.divergia.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AceitarSugestaoRequest(@NotBlank(message = "texto é obrigatório") String texto) {
}
