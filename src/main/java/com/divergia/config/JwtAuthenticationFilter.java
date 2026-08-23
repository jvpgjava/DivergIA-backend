package com.divergia.config;

import com.divergia.application.port.out.TokenPort;
import com.divergia.application.port.out.TokenRevogadoPort;
import com.divergia.domain.model.TokenAcesso;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Extrai e valida o JWT do header {@code Authorization: Bearer ...}, checando
 * também a lista de revogação (logout real, mesmo com JWT stateless). Um
 * token ausente/inválido/expirado/revogado simplesmente segue sem
 * autenticação — quem decide se isso é aceitável é o
 * {@code authorizeHttpRequests} do {@link SecurityConfig}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenPort tokenPort;
    private final TokenRevogadoPort tokenRevogadoPort;

    public JwtAuthenticationFilter(TokenPort tokenPort, TokenRevogadoPort tokenRevogadoPort) {
        this.tokenPort = tokenPort;
        this.tokenRevogadoPort = tokenRevogadoPort;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            String tokenBruto = cabecalho.substring("Bearer ".length());
            try {
                TokenAcesso token = tokenPort.validar(tokenBruto);
                if (!tokenRevogadoPort.estaRevogado(token.jti())) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            token.usuarioId(), null, List.of(new SimpleGrantedAuthority("ROLE_USUARIO")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (IllegalArgumentException tokenInvalido) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
