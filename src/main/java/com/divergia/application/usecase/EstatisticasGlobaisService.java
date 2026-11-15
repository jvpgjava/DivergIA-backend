package com.divergia.application.usecase;

import com.divergia.application.port.in.ObterEstatisticasGlobaisUseCase;
import com.divergia.application.port.out.MetricasGlobaisPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.EstatisticasGlobais;
import com.divergia.domain.model.FusoDasMetricas;
import com.divergia.domain.model.PontoDiarioGlobal;
import com.divergia.domain.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EstatisticasGlobaisService implements ObterEstatisticasGlobaisUseCase {

    public static final int DIAS_MINIMOS = 1;
    public static final int DIAS_MAXIMOS = 365;

    private final UsuarioRepositoryPort usuarioRepository;
    private final MetricasGlobaisPort metricas;
    private final Clock relogio;

    @Autowired
    public EstatisticasGlobaisService(UsuarioRepositoryPort usuarioRepository, MetricasGlobaisPort metricas) {
        this(usuarioRepository, metricas, Clock.systemUTC());
    }

    EstatisticasGlobaisService(UsuarioRepositoryPort usuarioRepository, MetricasGlobaisPort metricas, Clock relogio) {
        this.usuarioRepository = usuarioRepository;
        this.metricas = metricas;
        this.relogio = relogio;
    }

    @Override
    public EstatisticasGlobais obter(UUID solicitanteId, int dias) {
        // O papel é lido do banco a cada chamada: promover ou rebaixar vale na hora, sem esperar o token expirar.
        boolean ehDev = usuarioRepository.buscarPorId(solicitanteId).map(Usuario::ehDev).orElse(false);
        if (!ehDev) {
            throw new AcessoNaoAutorizadoException();
        }

        int janela = Math.clamp(dias, DIAS_MINIMOS, DIAS_MAXIMOS);
        Instant agora = relogio.instant();
        LocalDate hoje = FusoDasMetricas.diaDe(agora);
        LocalDate primeiroDia = hoje.minusDays(janela - 1L);

        EstatisticasGlobais bruto = metricas.consultar(primeiroDia, agora);
        return new EstatisticasGlobais(
                bruto.totalAnalises(),
                bruto.totalDerivas(),
                bruto.derivasPorTipo(),
                completarDias(bruto.serieDiaria(), primeiroDia, hoje),
                bruto.geradoEm());
    }

    // O gráfico precisa de um ponto por dia, inclusive nos dias sem nenhuma análise.
    private static List<PontoDiarioGlobal> completarDias(
            List<PontoDiarioGlobal> pontos, LocalDate primeiro, LocalDate ultimo) {
        Map<LocalDate, PontoDiarioGlobal> porDia = pontos.stream()
                .collect(Collectors.toMap(PontoDiarioGlobal::dia, Function.identity(), (a, b) -> a));
        List<PontoDiarioGlobal> serie = new ArrayList<>();
        for (LocalDate dia = primeiro; !dia.isAfter(ultimo); dia = dia.plusDays(1)) {
            serie.add(porDia.getOrDefault(dia, PontoDiarioGlobal.vazio(dia)));
        }
        return serie;
    }
}
