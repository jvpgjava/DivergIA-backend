package com.divergia.application.usecase;

import com.divergia.application.port.in.AlterarSenhaUseCase;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AlterarSenhaService implements AlterarSenhaUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;

    public AlterarSenhaService(UsuarioRepositoryPort usuarioRepository, PasswordEncoderPort passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void alterar(UUID usuarioId, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
        if (!passwordEncoder.confere(senhaAtual, usuario.senhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        Usuario atualizado = new Usuario(
                usuario.id(),
                usuario.nome(),
                usuario.email(),
                passwordEncoder.codificar(novaSenha),
                usuario.criadoEm(),
                usuario.fotoUrl());
        usuarioRepository.salvar(atualizado);
    }
}
