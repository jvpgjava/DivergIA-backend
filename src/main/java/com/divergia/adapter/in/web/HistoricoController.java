package com.divergia.adapter.in.web;

import com.divergia.adapter.in.web.dto.AnaliseResumoResponse;
import com.divergia.adapter.in.web.dto.PainelTendenciaResponse;
import com.divergia.adapter.in.web.dto.ResultadoAnaliseResponse;
import com.divergia.application.port.in.BuscarAnaliseUseCase;
import com.divergia.application.port.in.ExcluirAnaliseUseCase;
import com.divergia.application.port.in.ExcluirHistoricoUseCase;
import com.divergia.application.port.in.ListarHistoricoUseCase;
import com.divergia.application.port.in.ObterPainelTendenciaUseCase;
import com.divergia.domain.model.ResultadoAnalise;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/historico")
@Tag(name = "Histórico", description = "Histórico de análises, painel de tendência e exclusão de dados")
public class HistoricoController {

    private final ListarHistoricoUseCase listarHistorico;
    private final BuscarAnaliseUseCase buscarAnalise;
    private final ExcluirAnaliseUseCase excluirAnalise;
    private final ExcluirHistoricoUseCase excluirHistorico;
    private final ObterPainelTendenciaUseCase obterPainelTendencia;

    public HistoricoController(
            ListarHistoricoUseCase listarHistorico,
            BuscarAnaliseUseCase buscarAnalise,
            ExcluirAnaliseUseCase excluirAnalise,
            ExcluirHistoricoUseCase excluirHistorico,
            ObterPainelTendenciaUseCase obterPainelTendencia) {
        this.listarHistorico = listarHistorico;
        this.buscarAnalise = buscarAnalise;
        this.excluirAnalise = excluirAnalise;
        this.excluirHistorico = excluirHistorico;
        this.obterPainelTendencia = obterPainelTendencia;
    }

    @GetMapping
    public List<AnaliseResumoResponse> listar(@AuthenticationPrincipal UUID usuarioId) {
        return listarHistorico.listar(usuarioId).stream().map(AnaliseResumoResponse::from).toList();
    }

    @GetMapping("/tendencia")
    public PainelTendenciaResponse tendencia(@AuthenticationPrincipal UUID usuarioId) {
        return PainelTendenciaResponse.from(obterPainelTendencia.obter(usuarioId));
    }

    @GetMapping("/{analiseId:[0-9a-fA-F-]{36}}")
    public ResultadoAnaliseResponse buscar(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID analiseId) {
        ResultadoAnalise resultado = buscarAnalise.buscar(usuarioId, analiseId);
        return ResultadoAnaliseResponse.from(resultado);
    }

    @DeleteMapping("/{analiseId:[0-9a-fA-F-]{36}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal UUID usuarioId, @PathVariable UUID analiseId) {
        excluirAnalise.excluir(usuarioId, analiseId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirTudo(@AuthenticationPrincipal UUID usuarioId) {
        excluirHistorico.excluirTudo(usuarioId);
    }
}
