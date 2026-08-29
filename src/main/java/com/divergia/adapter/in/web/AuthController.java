package com.divergia.adapter.in.web;

import com.divergia.adapter.in.web.dto.CadastroRequest;
import com.divergia.adapter.in.web.dto.LoginRequest;
import com.divergia.adapter.in.web.dto.LoginResponse;
import com.divergia.adapter.in.web.dto.RecuperarSenhaRequest;
import com.divergia.adapter.in.web.dto.RedefinirSenhaRequest;
import com.divergia.adapter.in.web.dto.UsuarioResponse;
import com.divergia.application.port.in.AutenticarUsuarioUseCase;
import com.divergia.application.port.in.CadastrarUsuarioUseCase;
import com.divergia.application.port.in.EncerrarSessaoUseCase;
import com.divergia.application.port.in.ExcluirContaUseCase;
import com.divergia.application.port.in.ObterUsuarioLogadoUseCase;
import com.divergia.application.port.in.RedefinirSenhaUseCase;
import com.divergia.application.port.in.SolicitarRecuperacaoSenhaUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Cadastro, login, logout, recuperação de senha e exclusão de conta")
public class AuthController {

    private final CadastrarUsuarioUseCase cadastrarUsuario;
    private final AutenticarUsuarioUseCase autenticarUsuario;
    private final EncerrarSessaoUseCase encerrarSessao;
    private final SolicitarRecuperacaoSenhaUseCase solicitarRecuperacaoSenha;
    private final RedefinirSenhaUseCase redefinirSenha;
    private final ExcluirContaUseCase excluirConta;
    private final ObterUsuarioLogadoUseCase obterUsuarioLogado;

    public AuthController(
            CadastrarUsuarioUseCase cadastrarUsuario,
            AutenticarUsuarioUseCase autenticarUsuario,
            EncerrarSessaoUseCase encerrarSessao,
            SolicitarRecuperacaoSenhaUseCase solicitarRecuperacaoSenha,
            RedefinirSenhaUseCase redefinirSenha,
            ExcluirContaUseCase excluirConta,
            ObterUsuarioLogadoUseCase obterUsuarioLogado) {
        this.cadastrarUsuario = cadastrarUsuario;
        this.autenticarUsuario = autenticarUsuario;
        this.encerrarSessao = encerrarSessao;
        this.solicitarRecuperacaoSenha = solicitarRecuperacaoSenha;
        this.redefinirSenha = redefinirSenha;
        this.excluirConta = excluirConta;
        this.obterUsuarioLogado = obterUsuarioLogado;
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

    private String extrairToken(String cabecalhoAutorizacao) {
        if (cabecalhoAutorizacao == null || !cabecalhoAutorizacao.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Cabeçalho Authorization ausente ou mal formado");
        }
        return cabecalhoAutorizacao.substring("Bearer ".length());
    }
}
