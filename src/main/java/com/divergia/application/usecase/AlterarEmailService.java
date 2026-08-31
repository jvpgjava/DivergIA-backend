package com.divergia.application.usecase;

import com.divergia.application.port.in.AlterarEmailUseCase;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AlterarEmailService implements AlterarEmailUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;

    public AlterarEmailService(UsuarioRepositoryPort usuarioRepository, PasswordEncoderPort passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario alterar(UUID usuarioId, String novoEmail, String senhaAtual) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
        if (!passwordEncoder.confere(senhaAtual, usuario.senhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        if (!usuario.email().equalsIgnoreCase(novoEmail) && usuarioRepository.existeComEmail(novoEmail)) {
            throw new EmailJaCadastradoException(novoEmail);
        }
        Usuario atualizado = new Usuario(
                usuario.id(), usuario.nome(), novoEmail, usuario.senhaHash(), usuario.criadoEm(), usuario.fotoUrl());
        return usuarioRepository.salvar(atualizado);
    }
}
