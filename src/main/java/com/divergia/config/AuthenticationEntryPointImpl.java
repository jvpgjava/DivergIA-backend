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

@Component
// 401, e não o 403 padrão do Spring Security: o app só reconhece sessão expirada pelo 401.
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        // Antes de getWriter(): sem charset o Servlet usa ISO-8859-1 e corrompe os acentos.
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String corpo =
                """
                {"timestamp":"%s","status":401,"error":"Unauthorized","message":"Sessão expirada ou token inválido. Faça login novamente.","path":"%s"}"""
                        .formatted(Instant.now(), request.getRequestURI());
        response.getWriter().write(corpo);
    }
}
