package com.divergia.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 100, message = "nome deve ter no máximo 100 caracteres")
        @Pattern(regexp = Limites.NOME, message = "nome deve ter só letras, números, espaço, ponto, apóstrofo ou hífen")
                String nome,
        @NotBlank(message = "email é obrigatório")
        @Size(max = Limites.EMAIL, message = "email muito longo")
        @Email(message = "email inválido") String email,
        @NotBlank(message = "senha é obrigatória")
        @Size(min = Limites.SENHA_MINIMO, max = Limites.SENHA_MAXIMO, message = "senha deve ter entre 8 e 64 caracteres")
                String senha) {
}
