package com.divergia.application.usecase;

import com.divergia.application.port.in.EntrarComGoogleUseCase;
import com.divergia.application.port.out.EmailPort;
import com.divergia.application.port.out.PasswordEncoderPort;
import com.divergia.application.port.out.TokenPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.application.port.out.VerificadorGooglePort;
import com.divergia.domain.model.IdentidadeGoogle;
import com.divergia.domain.model.TokenAcesso;
import com.divergia.domain.model.Usuario;
import com.divergia.domain.service.EmailNormalizado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

@Service
public class EntrarComGoogleService implements EntrarComGoogleUseCase {

    private static final Logger log = LoggerFactory.getLogger(EntrarComGoogleService.class);
    private static final Pattern FORA_DO_NOME = Pattern.compile("[^\\p{L}\\p{M}\\p{N} .'-]");
    private static final int MAXIMO_NOME = 100;

    private final VerificadorGooglePort verificador;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenPort tokenPort;
    private final EmailPort emailPort;
    private final SecureRandom aleatorio = new SecureRandom();

    public EntrarComGoogleService(
            VerificadorGooglePort verificador,
            UsuarioRepositoryPort usuarioRepository,
            PasswordEncoderPort passwordEncoder,
            TokenPort tokenPort,
            EmailPort emailPort) {
        this.verificador = verificador;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenPort = tokenPort;
        this.emailPort = emailPort;
    }

    @Override
    public TokenAcesso entrar(String idToken) {
        if (!verificador.configurado()) {
            throw new LoginGoogleIndisponivelException();
        }
        IdentidadeGoogle identidade = verificador.verificar(idToken)
                .orElseThrow(LoginGoogleInvalidoException::new);
        String email = EmailNormalizado.de(identidade.email());

        // E-mail verificado pelo Google: entra na conta existente com esse e-mail (mantém o histórico)
        // ou cria uma nova na hora.
        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseGet(() -> criarConta(email, identidade.nome()));
        return tokenPort.gerar(usuario.id());
    }

    private Usuario criarConta(String email, String nomeGoogle) {
        // Senha aleatória que ninguém conhece: a conta entra pelo Google e pode definir
        // uma senha depois pelo "Esqueci minha senha".
        String senhaAleatoria = Base64.getUrlEncoder().encodeToString(aleatorio.generateSeed(24));
        Usuario salvo = usuarioRepository.salvar(
                Usuario.novo(nomeSeguro(nomeGoogle, email), email, passwordEncoder.codificar(senhaAleatoria)));
        log.info("Conta criada via Google para o usuário {}", salvo.id());
        try {
            emailPort.enviarBoasVindas(salvo.email(), salvo.nome());
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail de boas-vindas ao usuário {}: {}", salvo.id(), e.getMessage());
        }
        return salvo;
    }

    static String nomeSeguro(String nomeGoogle, String email) {
        String nome = nomeGoogle == null ? "" : FORA_DO_NOME.matcher(nomeGoogle).replaceAll("").replaceAll(" +", " ").strip();
        if (nome.isEmpty()) {
            nome = FORA_DO_NOME.matcher(email.substring(0, email.indexOf('@'))).replaceAll("").strip();
        }
        if (nome.isEmpty()) {
            nome = "Usuário";
        }
        return nome.length() > MAXIMO_NOME ? nome.substring(0, MAXIMO_NOME).strip() : nome;
    }
}
