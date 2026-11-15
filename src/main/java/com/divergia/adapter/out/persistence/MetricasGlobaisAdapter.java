package com.divergia.adapter.out.persistence;

import com.divergia.application.port.out.MetricasGlobaisPort;
import com.divergia.domain.model.EstatisticasGlobais;
import com.divergia.domain.model.FusoDasMetricas;
import com.divergia.domain.model.PontoDiarioGlobal;
import com.divergia.domain.model.TipoDesvio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class MetricasGlobaisAdapter implements MetricasGlobaisPort {

    // Um UPSERT atômico: duas análises simultâneas no mesmo dia somam, nenhuma sobrescreve a outra.
    private static final String SQL_REGISTRAR = """
            INSERT INTO metrica_analise_diaria (dia, analises, derivas_sentido, derivas_posicao, derivas_intensidade)
            VALUES (?, 1, ?, ?, ?)
            ON CONFLICT (dia) DO UPDATE SET
                analises = metrica_analise_diaria.analises + 1,
                derivas_sentido = metrica_analise_diaria.derivas_sentido + EXCLUDED.derivas_sentido,
                derivas_posicao = metrica_analise_diaria.derivas_posicao + EXCLUDED.derivas_posicao,
                derivas_intensidade = metrica_analise_diaria.derivas_intensidade + EXCLUDED.derivas_intensidade
            """;

    private static final String SQL_TOTAIS = """
            SELECT COALESCE(SUM(analises), 0), COALESCE(SUM(derivas_sentido), 0),
                   COALESCE(SUM(derivas_posicao), 0), COALESCE(SUM(derivas_intensidade), 0)
            FROM metrica_analise_diaria
            """;

    private static final String SQL_SERIE = """
            SELECT dia, analises, derivas_sentido, derivas_posicao, derivas_intensidade
            FROM metrica_analise_diaria
            WHERE dia >= ?
            ORDER BY dia
            """;

    private final JdbcTemplate jdbc;

    public MetricasGlobaisAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void registrarAnalise(Instant momento, List<TipoDesvio> derivas) {
        jdbc.update(
                SQL_REGISTRAR,
                Date.valueOf(FusoDasMetricas.diaDe(momento)),
                contar(derivas, TipoDesvio.SENTIDO),
                contar(derivas, TipoDesvio.POSICAO),
                contar(derivas, TipoDesvio.INTENSIDADE));
    }

    @Override
    public EstatisticasGlobais consultar(LocalDate serieDesde, Instant geradoEm) {
        EstatisticasGlobais totais = jdbc.queryForObject(SQL_TOTAIS, (rs, i) -> {
            long sentido = rs.getLong(2);
            long posicao = rs.getLong(3);
            long intensidade = rs.getLong(4);
            return new EstatisticasGlobais(
                    rs.getLong(1), sentido + posicao + intensidade, porTipo(sentido, posicao, intensidade),
                    List.of(), geradoEm);
        });
        List<PontoDiarioGlobal> serie = jdbc.query(
                SQL_SERIE,
                (rs, i) -> new PontoDiarioGlobal(
                        rs.getDate(1).toLocalDate(),
                        rs.getLong(2),
                        porTipo(rs.getLong(3), rs.getLong(4), rs.getLong(5))),
                Date.valueOf(serieDesde));
        return new EstatisticasGlobais(
                totais.totalAnalises(), totais.totalDerivas(), totais.derivasPorTipo(), serie, geradoEm);
    }

    private static long contar(List<TipoDesvio> derivas, TipoDesvio tipo) {
        return derivas.stream().filter(tipo::equals).count();
    }

    private static Map<TipoDesvio, Long> porTipo(long sentido, long posicao, long intensidade) {
        Map<TipoDesvio, Long> mapa = new EnumMap<>(TipoDesvio.class);
        mapa.put(TipoDesvio.SENTIDO, sentido);
        mapa.put(TipoDesvio.POSICAO, posicao);
        mapa.put(TipoDesvio.INTENSIDADE, intensidade);
        return mapa;
    }
}
