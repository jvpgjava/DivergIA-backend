package com.divergia.application.usecase;

import com.divergia.application.port.in.CadastrarUsuarioUseCase;
import com.divergia.application.port.out.EmailPort;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CadastrarUsuarioService implements CadastrarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final EmailPort emailPort;

    public CadastrarUsuarioService(
            UsuarioRepositoryPort usuarioRepository, PasswordEncoderPort passwordEncoder, EmailPort emailPort) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailPort = emailPort;
    }

    @Override
    public Usuario cadastrar(String nome, String email, String senha) {
        if (usuarioRepository.existeComEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }
        Usuario usuario = new Usuario(
                UUID.randomUUID(), nome, email, passwordEncoder.codificar(senha), Instant.now(), null);
        Usuario salvo = usuarioRepository.salvar(usuario);
        emailPort.enviarBoasVindas(salvo.email(), salvo.nome());
        return salvo;
    }
}
