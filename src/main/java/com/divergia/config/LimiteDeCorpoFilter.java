package com.divergia.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LimiteDeCorpoFilter extends OncePerRequestFilter {

    private final long corpoMaximoBytes;

    public LimiteDeCorpoFilter(@Value("${divergia.corpo-maximo-bytes:262144}") long corpoMaximoBytes) {
        this.corpoMaximoBytes = corpoMaximoBytes;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (ehUploadMultipart(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        long tamanho = request.getContentLengthLong();
        boolean temCorpo = tamanho > 0 || request.getHeader("Transfer-Encoding") != null;
        // Corpo sem tamanho declarado (chunked) não dá para limitar antes de ler: recusa.
        if (temCorpo && (tamanho < 0 || tamanho > corpoMaximoBytes)) {
            response.setStatus(HttpStatus.CONTENT_TOO_LARGE.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":413,\"error\":\"Content Too Large\",\"message\":\"Requisição grande demais.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean ehUploadMultipart(HttpServletRequest request) {
        String tipo = request.getContentType();
        return tipo != null && tipo.toLowerCase().startsWith(MediaType.MULTIPART_FORM_DATA_VALUE);
    }
}
