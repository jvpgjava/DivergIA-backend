package com.divergia.application.usecase;

import com.divergia.application.port.in.CadastrarUsuarioUseCase;
import com.divergia.application.port.out.EmailPort;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CadastrarUsuarioService implements CadastrarUsuarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(CadastrarUsuarioService.class);

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
      
        try {
            emailPort.enviarBoasVindas(salvo.email(), salvo.nome());
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de boas-vindas para {}: {}", salvo.email(), e.getMessage());
        }
        return salvo;
    }
}
