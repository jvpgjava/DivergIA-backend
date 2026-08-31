package com.divergia.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Sem isso, o Spring Security devolve 403 (não 401) pra qualquer requisição
 * sem token, com token inválido/expirado ou revogado — o
 * {@link JwtAuthenticationFilter} só deixa a requisição seguir sem
 * autenticação, quem rejeita é o {@code authorizeHttpRequests}, e o
 * comportamento padrão do Spring Security nesse caso é 403.
 *
 * Isso quebra o app: tanto o {@code AuthInterceptor} (que só limpa a sessão
 * salva em 401) quanto o {@code ErrorInterceptor} (que só mostra "Sessão
 * expirada" em 401) esperam 401 especificamente pra esse cenário — com 403
 * o app nunca detecta a sessão expirada e fica preso mostrando "Você não
 * tem permissão para essa ação." em vez de mandar a pessoa pro login de
 * novo.
 *
 * Monta o JSON à mão (mesmo shape de {@code ErroResponse}) em vez de injetar
 * um {@code ObjectMapper} — esse bean nem sempre está disponível nas fatias
 * de teste (`@WebMvcTest`) que carregam o filtro de segurança.
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        // `getWriter()` usa ISO-8859-1 por padrão (spec do Servlet) se o
        // charset não for setado ANTES de pegar o writer — sem isso, os
        // acentos da mensagem saem corrompidos no corpo da resposta.
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String corpo =
                """
                {"timestamp":"%s","status":401,"error":"Unauthorized","message":"Sessão expirada ou token inválido. Faça login novamente.","path":"%s"}"""
                        .formatted(Instant.now(), request.getRequestURI());
        response.getWriter().write(corpo);
    }
}
