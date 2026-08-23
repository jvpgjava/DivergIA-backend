package com.divergia.application.usecase;

import com.divergia.application.port.in.RedefinirSenhaUseCase;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.TokenRecuperacaoSenhaRepositoryPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.TokenRecuperacaoSenha;
import com.divergia.domain.model.Usuario;
import com.divergia.domain.service.GeradorDeTokenSeguro;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RedefinirSenhaService implements RedefinirSenhaUseCase {

    private final TokenRecuperacaoSenhaRepositoryPort tokenRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final GeradorDeTokenSeguro gerador = new GeradorDeTokenSeguro();

    public RedefinirSenhaService(
            TokenRecuperacaoSenhaRepositoryPort tokenRepository,
            UsuarioRepositoryPort usuarioRepository,
            PasswordEncoderPort passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void redefinir(String tokenBruto, String novaSenha) {
        String hash = gerador.hash(tokenBruto);
        TokenRecuperacaoSenha token = tokenRepository.buscarPorHash(hash)
                .filter(t -> t.valido(Instant.now()))
                .orElseThrow(TokenInvalidoOuExpiradoException::new);

        Usuario usuario = usuarioRepository.buscarPorId(token.usuarioId())
                .orElseThrow(TokenInvalidoOuExpiradoException::new);

        Usuario atualizado = new Usuario(
                usuario.id(), usuario.nome(), usuario.email(),
                passwordEncoder.codificar(novaSenha), usuario.criadoEm());

        usuarioRepository.salvar(atualizado);
        tokenRepository.marcarComoUsado(token.id());
    }
}
