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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting em memória (por IP) para login e cadastro — alvo natural de
 * força bruta e abuso. Em memória porque a infraestrutura deste projeto é
 * uma única instância de VPS, sem Redis/cache distribuído.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Set<String> ROTAS_LIMITADAS = Set.of("/api/auth/login", "/api/auth/cadastro");

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int capacidade;
    private final Duration janela;

    public RateLimitingFilter(
            @Value("${divergia.rate-limit.auth.capacidade:5}") int capacidade,
            @Value("${divergia.rate-limit.auth.janela-minutos:1}") long janelaMinutos) {
        this.capacidade = capacidade;
        this.janela = Duration.ofMinutes(janelaMinutos);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod()) && ROTAS_LIMITADAS.contains(request.getRequestURI())) {
            Bucket bucket = buckets.computeIfAbsent(request.getRemoteAddr(), ip -> criarBucket());
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private Bucket criarBucket() {
        return Bucket.builder()
                .addLimit(limite -> limite.capacity(capacidade).refillGreedy(capacidade, janela))
                .build();
    }
}
