package com.divergia.application.usecase;

import com.divergia.application.port.in.AutenticarUsuarioUseCase;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.TokenPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.TokenAcesso;
import com.divergia.domain.model.Usuario;
import com.divergia.domain.service.EmailNormalizado;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenPort tokenPort;
    private final String hashFicticio;

    public AutenticarUsuarioService(
            UsuarioRepositoryPort usuarioRepository, PasswordEncoderPort passwordEncoder, TokenPort tokenPort) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenPort = tokenPort;
        this.hashFicticio = passwordEncoder.codificar("senha-ficticia-para-tempo-constante");
    }

    @Override
    public TokenAcesso autenticar(String email, String senha) {
        Optional<Usuario> encontrado = usuarioRepository.buscarPorEmail(EmailNormalizado.de(email));
        // Compara com um hash fictício quando o e-mail não existe: o tempo de resposta não revela contas.
        String hash = encontrado.map(Usuario::senhaHash).orElse(hashFicticio);
        boolean confere = passwordEncoder.confere(senha, hash);
        Usuario usuario = encontrado.filter(u -> confere).orElseThrow(CredenciaisInvalidasException::new);
        return tokenPort.gerar(usuario.id());
    }
}
