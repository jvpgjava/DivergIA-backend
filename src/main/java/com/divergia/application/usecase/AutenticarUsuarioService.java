package com.divergia.application.usecase;

import com.divergia.application.port.in.AutenticarUsuarioUseCase;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.TokenPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.TokenAcesso;
import com.divergia.domain.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenPort tokenPort;

    public AutenticarUsuarioService(
            UsuarioRepositoryPort usuarioRepository, PasswordEncoderPort passwordEncoder, TokenPort tokenPort) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenPort = tokenPort;
    }

    @Override
    public TokenAcesso autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.confere(senha, usuario.senhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        return tokenPort.gerar(usuario.id());
    }
}
