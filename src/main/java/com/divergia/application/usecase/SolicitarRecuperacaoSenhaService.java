package com.divergia.application.usecase;

import com.divergia.application.port.in.SolicitarRecuperacaoSenhaUseCase;
import com.divergia.application.port.out.EmailPort;
import com.divergia.application.port.out.TokenRecuperacaoSenhaRepositoryPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.TokenRecuperacaoSenha;
import com.divergia.domain.model.Usuario;
import com.divergia.domain.service.GeradorDeTokenSeguro;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class SolicitarRecuperacaoSenhaService implements SolicitarRecuperacaoSenhaUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final TokenRecuperacaoSenhaRepositoryPort tokenRepository;
    private final EmailPort emailPort;
    private final GeradorDeTokenSeguro gerador = new GeradorDeTokenSeguro();
    private final Duration validade;

    public SolicitarRecuperacaoSenhaService(
            UsuarioRepositoryPort usuarioRepository,
            TokenRecuperacaoSenhaRepositoryPort tokenRepository,
            EmailPort emailPort,
            @Value("${divergia.recuperacao-senha.validade-minutos:30}") long validadeMinutos) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.emailPort = emailPort;
        this.validade = Duration.ofMinutes(validadeMinutos);
    }

    @Override
    public void solicitar(String email) {
        Optional<Usuario> usuario = usuarioRepository.buscarPorEmail(email);
        if (usuario.isEmpty()) {
            return;
        }

        String tokenBruto = gerador.gerar();
        Instant agora = Instant.now();
        TokenRecuperacaoSenha token = new TokenRecuperacaoSenha(
                UUID.randomUUID(), usuario.get().id(), gerador.hash(tokenBruto), agora, agora.plus(validade), null);

        tokenRepository.salvar(token);
        emailPort.enviarRecuperacaoSenha(email, tokenBruto);
    }
}
