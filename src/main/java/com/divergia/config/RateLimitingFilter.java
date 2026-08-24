package com.divergia.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting em memória (por IP) para login/cadastro (alvo natural de
 * força bruta) e para o endpoint de análise (cada chamada tem custo real de
 * LLM). Em memória porque a infraestrutura deste projeto é uma única
 * instância de VPS, sem Redis/cache distribuído.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private record Regra(String metodo, String caminho, int capacidade, Duration janela) {

        boolean combinaCom(HttpServletRequest request) {
            return metodo.equalsIgnoreCase(request.getMethod()) && caminho.equals(request.getRequestURI());
        }
    }

    private final List<Regra> regras;
    private final Map<Regra, Map<String, Bucket>> bucketsPorRegra = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${divergia.rate-limit.auth.capacidade:5}") int authCapacidade,
            @Value("${divergia.rate-limit.auth.janela-minutos:1}") long authJanelaMinutos,
            @Value("${divergia.rate-limit.analise.capacidade:10}") int analiseCapacidade,
            @Value("${divergia.rate-limit.analise.janela-minutos:1}") long analiseJanelaMinutos) {
        this.regras = List.of(
                new Regra("POST", "/api/auth/login", authCapacidade, Duration.ofMinutes(authJanelaMinutos)),
                new Regra("POST", "/api/auth/cadastro", authCapacidade, Duration.ofMinutes(authJanelaMinutos)),
                new Regra("POST", "/api/analises", analiseCapacidade, Duration.ofMinutes(analiseJanelaMinutos)));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        for (Regra regra : regras) {
            if (regra.combinaCom(request)) {
                Map<String, Bucket> buckets = bucketsPorRegra.computeIfAbsent(regra, r -> new ConcurrentHashMap<>());
                Bucket bucket = buckets.computeIfAbsent(request.getRemoteAddr(), ip -> criarBucket(regra));
                if (!bucket.tryConsume(1)) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    return;
                }
                break;
            }
        }
        filterChain.doFilter(request, response);
    }

    private Bucket criarBucket(Regra regra) {
        return Bucket.builder()
                .addLimit(limite -> limite.capacity(regra.capacidade()).refillGreedy(regra.capacidade(), regra.janela()))
                .build();
    }
}
