package com.divergia.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Teto de IPs em memória: sem ele, um atacante trocando de IP esgota a memória do servidor.
    static final int MAXIMO_IPS_POR_REGRA = 50_000;

    private record Regra(String metodo, Pattern caminho, int capacidade, Duration janela) {

        boolean combinaCom(HttpServletRequest request) {
            return (metodo == null || metodo.equalsIgnoreCase(request.getMethod()))
                    && caminho.matcher(request.getRequestURI()).matches();
        }
    }

    private final List<Regra> regras;
    private final Regra regraGeral;
    private final Map<Regra, Map<String, Bucket>> bucketsPorRegra = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${divergia.rate-limit.auth.capacidade:5}") int authCapacidade,
            @Value("${divergia.rate-limit.auth.janela-minutos:1}") long authJanelaMinutos,
            @Value("${divergia.rate-limit.analise.capacidade:10}") int analiseCapacidade,
            @Value("${divergia.rate-limit.analise.janela-minutos:1}") long analiseJanelaMinutos,
            @Value("${divergia.rate-limit.geral.capacidade:120}") int geralCapacidade) {
        Duration auth = Duration.ofMinutes(authJanelaMinutos);
        Duration analise = Duration.ofMinutes(analiseJanelaMinutos);
        this.regras = List.of(
                regra("POST", "/api/auth/login", authCapacidade, auth),
                regra("POST", "/api/auth/cadastro", authCapacidade, auth),
                regra("POST", "/api/auth/google", authCapacidade, auth),
                // O código de recuperação tem só 6 caracteres: sem limite, cabe força bruta.
                regra("POST", "/api/auth/redefinir-senha", authCapacidade, auth),
                regra("POST", "/api/auth/recuperar-senha", authCapacidade, auth),
                regra("PUT", "/api/auth/senha", authCapacidade, auth),
                regra("PUT", "/api/auth/email", authCapacidade, auth),
                regra("POST", "/api/auth/foto", authCapacidade, auth),
                regra("DELETE", "/api/auth/conta", authCapacidade, auth),
                regra("POST", "/api/analises", analiseCapacidade, analise),
                regra("POST", "/api/analises/trechos/[^/]+/sugestao-reescrita", analiseCapacidade, analise));
        this.regraGeral = regra(null, ".*", geralCapacidade, Duration.ofMinutes(1));
    }

    private static Regra regra(String metodo, String caminho, int capacidade, Duration janela) {
        return new Regra(metodo, Pattern.compile(caminho), capacidade, janela);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        if (!consumir(regraGeral, ip)) {
            recusar(response);
            return;
        }
        for (Regra regra : regras) {
            if (regra.combinaCom(request)) {
                if (!consumir(regra, ip)) {
                    recusar(response);
                    return;
                }
                break;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean consumir(Regra regra, String ip) {
        Map<String, Bucket> buckets = bucketsPorRegra.computeIfAbsent(regra, r -> new ConcurrentHashMap<>());
        if (buckets.size() >= MAXIMO_IPS_POR_REGRA && !buckets.containsKey(ip)) {
            buckets.clear();
        }
        return buckets.computeIfAbsent(ip, chave -> criarBucket(regra)).tryConsume(1);
    }

    private static void recusar(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", "60");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\","
                        + "\"message\":\"Muitas tentativas em pouco tempo. Aguarde um minuto e tente novamente.\"}");
    }

    private Bucket criarBucket(Regra regra) {
        return Bucket.builder()
                .addLimit(limite -> limite.capacity(regra.capacidade()).refillGreedy(regra.capacidade(), regra.janela()))
                .build();
    }
}
