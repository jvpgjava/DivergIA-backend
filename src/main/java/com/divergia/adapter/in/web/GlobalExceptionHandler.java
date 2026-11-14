package com.divergia.adapter.in.web;

import com.divergia.adapter.in.web.dto.ErroResponse;
import com.divergia.application.port.out.ExtracaoDocumentoException;
import com.divergia.application.port.out.LlmException;
import com.divergia.application.usecase.AcessoNaoAutorizadoException;
import com.divergia.application.usecase.AnaliseNaoEncontradaException;
import com.divergia.application.usecase.CredenciaisInvalidasException;
import com.divergia.application.usecase.EmailJaCadastradoException;
import com.divergia.application.usecase.EntradaInvalidaException;
import com.divergia.application.usecase.SugestaoIndisponivelException;
import com.divergia.application.usecase.TokenInvalidoOuExpiradoException;
import com.divergia.application.usecase.TrechoDerivaNaoEncontradoException;
import com.divergia.application.usecase.UsuarioNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.io.UncheckedIOException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> tratarEmailJaCadastrado(EmailJaCadastradoException e, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> tratarCredenciaisInvalidas(
            CredenciaisInvalidasException e, HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage(), request);
    }

    @ExceptionHandler(TokenInvalidoOuExpiradoException.class)
    public ResponseEntity<ErroResponse> tratarTokenInvalido(
            TokenInvalidoOuExpiradoException e, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(AnaliseNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> tratarAnaliseNaoEncontrada(
            AnaliseNaoEncontradaException e, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(TrechoDerivaNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarTrechoNaoEncontrado(
            TrechoDerivaNaoEncontradoException e, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarUsuarioNaoEncontrado(
            UsuarioNaoEncontradoException e, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(AcessoNaoAutorizadoException.class)
    public ResponseEntity<ErroResponse> tratarAcessoNaoAutorizado(
            AcessoNaoAutorizadoException e, HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, e.getMessage(), request);
    }

    @ExceptionHandler(ExtracaoDocumentoException.class)
    public ResponseEntity<ErroResponse> tratarFalhaDeExtracao(
            ExtracaoDocumentoException e, HttpServletRequest request) {
        log.warn("Falha ao extrair documento em {}: {}", request.getRequestURI(), e.getMessage());
        return responder(HttpStatus.BAD_GATEWAY, e.getMessage(), request);
    }

    @ExceptionHandler(LlmException.class)
    public ResponseEntity<ErroResponse> tratarFalhaDeLlm(LlmException e, HttpServletRequest request) {
        log.warn("Falha ao processar resposta do LLM em {}: {}", request.getRequestURI(), e.getMessage());
        return responder(
                HttpStatus.BAD_GATEWAY,
                "A análise não pôde ser concluída agora. Tente novamente em instantes.",
                request);
    }

    @ExceptionHandler(EntradaInvalidaException.class)
    public ResponseEntity<ErroResponse> tratarEntradaInvalida(EntradaInvalidaException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;
        return ResponseEntity.status(status).body(ErroResponse.of(
                status, e.getMessage(), request.getRequestURI(),
                e.problema().motivo().name(), e.problema().campo().name()));
    }

    @ExceptionHandler(SugestaoIndisponivelException.class)
    public ResponseEntity<ErroResponse> tratarSugestaoIndisponivel(
            SugestaoIndisponivelException e, HttpServletRequest request) {
        return responder(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), request);
    }

    @ExceptionHandler(UncheckedIOException.class)
    public ResponseEntity<ErroResponse> tratarFalhaDeLeituraDeArquivo(
            UncheckedIOException e, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroResponse> tratarArquivoMuitoGrande(
            MaxUploadSizeExceededException e, HttpServletRequest request) {
        return responder(
                HttpStatus.BAD_REQUEST,
                "O arquivo enviado é muito grande. O tamanho máximo permitido é 20MB.",
                request);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErroResponse> tratarFalhaDeUpload(MultipartException e, HttpServletRequest request) {
        log.warn("Falha ao processar upload multipart em {}: {}", request.getRequestURI(), e.getMessage());
        return responder(HttpStatus.BAD_REQUEST, "Não foi possível processar o arquivo enviado.", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> tratarArgumentoInvalido(IllegalArgumentException e, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException e, HttpServletRequest request) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception e, HttpServletRequest request) {
        log.error("Erro inesperado processando {} {}", request.getMethod(), request.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", request);
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ErroResponse.of(status, message, request.getRequestURI()));
    }
}
