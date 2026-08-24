package com.divergia.adapter.in.web;

import com.divergia.adapter.in.web.dto.ResultadoAnaliseResponse;
import com.divergia.adapter.in.web.dto.SugestaoReescritaResponse;
import com.divergia.application.port.in.AnalisarTextoUseCase;
import com.divergia.application.port.in.EntradaAnalise;
import com.divergia.application.port.in.EntradaTexto;
import com.divergia.application.port.in.SugerirReescritaUseCase;
import com.divergia.application.port.out.ExtracaoDocumentoException;
import com.divergia.application.usecase.AcessoNaoAutorizadoException;
import com.divergia.application.usecase.TrechoDerivaNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/analises")
public class AnaliseController {

    private final AnalisarTextoUseCase analisarTexto;
    private final SugerirReescritaUseCase sugerirReescrita;

    public AnaliseController(AnalisarTextoUseCase analisarTexto, SugerirReescritaUseCase sugerirReescrita) {
        this.analisarTexto = analisarTexto;
        this.sugerirReescrita = sugerirReescrita;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResultadoAnaliseResponse analisar(
            @AuthenticationPrincipal UUID usuarioId,
            @RequestParam(required = false) String textoOriginal,
            @RequestParam(required = false) MultipartFile arquivoOriginal,
            @RequestParam(required = false) String textoEditado,
            @RequestParam(required = false) MultipartFile arquivoEditado,
            @RequestParam(defaultValue = "false") boolean manterHistorico) {

        EntradaTexto original = paraEntradaTexto(textoOriginal, arquivoOriginal);
        EntradaTexto editado = paraEntradaTexto(textoEditado, arquivoEditado);

        return ResultadoAnaliseResponse.from(
                analisarTexto.analisar(new EntradaAnalise(usuarioId, original, editado, manterHistorico)));
    }

    @PostMapping("/trechos/{trechoId}/sugestao-reescrita")
    public SugestaoReescritaResponse sugerirReescrita(
            @AuthenticationPrincipal UUID usuarioId, @PathVariable UUID trechoId) {
        return new SugestaoReescritaResponse(sugerirReescrita.sugerir(usuarioId, trechoId));
    }

    private EntradaTexto paraEntradaTexto(String texto, MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            return EntradaTexto.deTexto(texto);
        }
        try {
            return EntradaTexto.deArquivo(arquivo.getBytes(), arquivo.getOriginalFilename());
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler o arquivo enviado: " + arquivo.getOriginalFilename(), e);
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarEntradaInvalida(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(ExtracaoDocumentoException.class)
    public ResponseEntity<String> tratarFalhaDeExtracao(ExtracaoDocumentoException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
    }

    @ExceptionHandler(UncheckedIOException.class)
    public ResponseEntity<String> tratarFalhaDeLeituraDeArquivo(UncheckedIOException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(TrechoDerivaNaoEncontradoException.class)
    public ResponseEntity<String> tratarTrechoNaoEncontrado(TrechoDerivaNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(AcessoNaoAutorizadoException.class)
    public ResponseEntity<String> tratarAcessoNaoAutorizado(AcessoNaoAutorizadoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
}
