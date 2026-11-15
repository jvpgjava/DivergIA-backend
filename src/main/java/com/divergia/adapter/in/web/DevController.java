package com.divergia.adapter.in.web;

import com.divergia.adapter.in.web.dto.EstatisticasGlobaisResponse;
import com.divergia.application.port.in.ObterEstatisticasGlobaisUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dev")
@Tag(name = "Dev", description = "Métricas globais, restritas ao papel DEV")
public class DevController {

    private final ObterEstatisticasGlobaisUseCase obterEstatisticas;

    public DevController(ObterEstatisticasGlobaisUseCase obterEstatisticas) {
        this.obterEstatisticas = obterEstatisticas;
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<EstatisticasGlobaisResponse> estatisticas(
            @AuthenticationPrincipal UUID usuarioId, @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(EstatisticasGlobaisResponse.from(obterEstatisticas.obter(usuarioId, dias)));
    }
}
