package com.divergia.adapter.in.web;

import com.divergia.adapter.in.web.dto.ConsentimentoRequest;
import com.divergia.adapter.in.web.dto.ConsentimentoResponse;
import com.divergia.application.port.in.AtualizarConsentimentoUseCase;
import com.divergia.application.port.in.ObterConsentimentoUseCase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/consentimento")
public class ConsentimentoController {

    private final ObterConsentimentoUseCase obterConsentimento;
    private final AtualizarConsentimentoUseCase atualizarConsentimento;

    public ConsentimentoController(
            ObterConsentimentoUseCase obterConsentimento, AtualizarConsentimentoUseCase atualizarConsentimento) {
        this.obterConsentimento = obterConsentimento;
        this.atualizarConsentimento = atualizarConsentimento;
    }

    @GetMapping
    public ConsentimentoResponse obter(@AuthenticationPrincipal UUID usuarioId) {
        return ConsentimentoResponse.from(obterConsentimento.obter(usuarioId));
    }

    @PutMapping
    public ConsentimentoResponse atualizar(
            @AuthenticationPrincipal UUID usuarioId, @RequestBody ConsentimentoRequest request) {
        return ConsentimentoResponse.from(
                atualizarConsentimento.atualizar(usuarioId, request.manterHistorico(), request.contribuirParaRag()));
    }
}
