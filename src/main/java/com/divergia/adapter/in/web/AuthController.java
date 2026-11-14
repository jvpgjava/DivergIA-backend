package com.divergia.adapter.in.web;

import com.divergia.adapter.in.web.dto.AlterarEmailRequest;
import com.divergia.adapter.in.web.dto.AlterarSenhaRequest;
import com.divergia.adapter.in.web.dto.CadastroRequest;
import com.divergia.adapter.in.web.dto.FotoPerfilResponse;
import com.divergia.adapter.in.web.dto.GoogleLoginRequest;
import com.divergia.adapter.in.web.dto.LoginRequest;
import com.divergia.adapter.in.web.dto.LoginResponse;
import com.divergia.adapter.in.web.dto.RecuperarSenhaRequest;
import com.divergia.adapter.in.web.dto.RedefinirSenhaRequest;
import com.divergia.adapter.in.web.dto.UsuarioResponse;
import com.divergia.application.port.in.AlterarEmailUseCase;
import com.divergia.application.port.in.AlterarSenhaUseCase;
import com.divergia.application.port.in.AtualizarFotoPerfilUseCase;
import com.divergia.application.port.in.AutenticarUsuarioUseCase;
import com.divergia.application.port.in.CadastrarUsuarioUseCase;
import com.divergia.application.port.in.EncerrarSessaoUseCase;
import com.divergia.application.port.in.EntrarComGoogleUseCase;
import com.divergia.application.port.in.ExcluirContaUseCase;
import com.divergia.application.port.in.ObterUsuarioLogadoUseCase;
import com.divergia.application.port.in.RedefinirSenhaUseCase;
import com.divergia.application.port.in.SolicitarRecuperacaoSenhaUseCase;
import com.divergia.domain.service.TipoDeImagem;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Cadastro, login, logout, recuperação de senha e exclusão de conta")
public class AuthController {

    private static final long TAMANHO_MAXIMO_FOTO = 5L * 1024 * 1024;

    private final CadastrarUsuarioUseCase cadastrarUsuario;
    private final AutenticarUsuarioUseCase autenticarUsuario;
    private final EncerrarSessaoUseCase encerrarSessao;
    private final SolicitarRecuperacaoSenhaUseCase solicitarRecuperacaoSenha;
    private final RedefinirSenhaUseCase redefinirSenha;
    private final ExcluirContaUseCase excluirConta;
    private final ObterUsuarioLogadoUseCase obterUsuarioLogado;
    private final AlterarSenhaUseCase alterarSenha;
    private final AlterarEmailUseCase alterarEmail;
    private final AtualizarFotoPerfilUseCase atualizarFotoPerfil;
    private final EntrarComGoogleUseCase entrarComGoogle;

    public AuthController(
            CadastrarUsuarioUseCase cadastrarUsuario,
            AutenticarUsuarioUseCase autenticarUsuario,
            EncerrarSessaoUseCase encerrarSessao,
            SolicitarRecuperacaoSenhaUseCase solicitarRecuperacaoSenha,
            RedefinirSenhaUseCase redefinirSenha,
            ExcluirContaUseCase excluirConta,
            ObterUsuarioLogadoUseCase obterUsuarioLogado,
            AlterarSenhaUseCase alterarSenha,
            AlterarEmailUseCase alterarEmail,
            AtualizarFotoPerfilUseCase atualizarFotoPerfil,
            EntrarComGoogleUseCase entrarComGoogle) {
        this.cadastrarUsuario = cadastrarUsuario;
        this.autenticarUsuario = autenticarUsuario;
        this.encerrarSessao = encerrarSessao;
        this.solicitarRecuperacaoSenha = solicitarRecuperacaoSenha;
        this.redefinirSenha = redefinirSenha;
        this.excluirConta = excluirConta;
        this.obterUsuarioLogado = obterUsuarioLogado;
        this.alterarSenha = alterarSenha;
        this.alterarEmail = alterarEmail;
        this.atualizarFotoPerfil = atualizarFotoPerfil;
        this.entrarComGoogle = entrarComGoogle;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest request) {
        return UsuarioResponse.from(cadastrarUsuario.cadastrar(request.nome(), request.email(), request.senha()));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return LoginResponse.from(autenticarUsuario.autenticar(request.email(), request.senha()));
    }

    @PostMapping("/google")
    public LoginResponse entrarComGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return LoginResponse.from(entrarComGoogle.entrar(request.idToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String cabecalhoAutorizacao) {
        encerrarSessao.encerrar(extrairToken(cabecalhoAutorizacao));
    }

    @PostMapping("/recuperar-senha")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest request) {
        solicitarRecuperacaoSenha.solicitar(request.email());
    }

    @PostMapping("/redefinir-senha")
    public void redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        redefinirSenha.redefinir(request.token(), request.novaSenha());
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal UUID usuarioId) {
        return UsuarioResponse.from(obterUsuarioLogado.obter(usuarioId));
    }

    @DeleteMapping("/conta")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirConta(@AuthenticationPrincipal UUID usuarioId) {
        excluirConta.excluir(usuarioId);
    }

    @PutMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@AuthenticationPrincipal UUID usuarioId, @Valid @RequestBody AlterarSenhaRequest request) {
        alterarSenha.alterar(usuarioId, request.senhaAtual(), request.novaSenha());
    }

    @PutMapping("/email")
    public UsuarioResponse alterarEmail(
            @AuthenticationPrincipal UUID usuarioId, @Valid @RequestBody AlterarEmailRequest request) {
        return UsuarioResponse.from(alterarEmail.alterar(usuarioId, request.novoEmail(), request.senhaAtual()));
    }

    @PostMapping(value = "/foto", consumes = "multipart/form-data")
    public FotoPerfilResponse atualizarFoto(
            @AuthenticationPrincipal UUID usuarioId, @RequestParam MultipartFile foto) {
        if (foto.isEmpty()) {
            throw new RequisicaoInvalidaException("Nenhuma foto foi enviada.");
        }
        if (foto.getSize() > TAMANHO_MAXIMO_FOTO) {
            throw new RequisicaoInvalidaException("A foto deve ter no máximo 5 MB.");
        }
        byte[] conteudo;
        try {
            conteudo = foto.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler a foto enviada", e);
        }
        TipoDeImagem tipo = TipoDeImagem.detectar(conteudo)
                .orElseThrow(() -> new RequisicaoInvalidaException("A foto deve ser uma imagem JPG, PNG ou WEBP."));
        return new FotoPerfilResponse(atualizarFotoPerfil.atualizar(usuarioId, conteudo, tipo.extensao()));
    }


    private String extrairToken(String cabecalhoAutorizacao) {
        if (cabecalhoAutorizacao == null || !cabecalhoAutorizacao.startsWith("Bearer ")) {
            throw new RequisicaoInvalidaException("Cabeçalho Authorization ausente ou mal formado.");
        }
        return cabecalhoAutorizacao.substring("Bearer ".length());
    }
}
