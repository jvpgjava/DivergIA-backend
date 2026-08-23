package com.divergia.application.port.in;

import com.divergia.domain.model.Usuario;

public interface CadastrarUsuarioUseCase {

    /**
     * @throws com.divergia.application.usecase.EmailJaCadastradoException se o e-mail já estiver em uso
     */
    Usuario cadastrar(String nome, String email, String senha);
}
