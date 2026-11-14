package com.divergia.application.port.in;

import com.divergia.domain.model.Usuario;

public interface CadastrarUsuarioUseCase {
    Usuario cadastrar(String nome, String email, String senha);
}
