package com.divergia.application.usecase;

import com.divergia.application.port.in.CadastrarUsuarioUseCase;
import com.divergia.application.port.out.EmailPort;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


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
        Usuario usuario = Usuario.novo(nome, email, passwordEncoder.codificar(senha));
        // E-mail já cadastrado responde igual a um cadastro novo (mesmo custo de BCrypt):
        // a resposta não pode revelar quais e-mails têm conta. A conta existente não é tocada.
        if (usuarioRepository.existeComEmail(email)) {
            log.info("Cadastro ignorado: e-mail já em uso");
            return usuario;
        }
        Usuario salvo = usuarioRepository.salvar(usuario);

        try {
            emailPort.enviarBoasVindas(salvo.email(), salvo.nome());
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de boas-vindas ao usuário {}: {}", salvo.id(), e.getMessage());
        }
        return salvo;
    }
}
