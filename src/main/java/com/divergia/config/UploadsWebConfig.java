package com.divergia.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Path;

@Configuration
public class UploadsWebConfig implements WebMvcConfigurer {

    private final String diretorio;

    public UploadsWebConfig(@Value("${divergia.uploads.diretorio}") String diretorio) {
        this.diretorio = diretorio;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String localizacao = "file:" + Path.of(diretorio).toAbsolutePath() + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(localizacao);
    }

    // Arquivo servido de /uploads nunca vira página: sem script, sem sniffing, baixado como anexo se não for imagem.
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> cabecalhosDeUploads() {
        OncePerRequestFilter filtro = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                response.setHeader("Content-Security-Policy", "default-src 'none'; sandbox");
                response.setHeader("X-Content-Type-Options", "nosniff");
                String caminho = request.getRequestURI().toLowerCase();
                if (!(caminho.endsWith(".jpg") || caminho.endsWith(".jpeg")
                        || caminho.endsWith(".png") || caminho.endsWith(".webp"))) {
                    response.setHeader("Content-Disposition", "attachment");
                }
                chain.doFilter(request, response);
            }
        };
        FilterRegistrationBean<OncePerRequestFilter> registro = new FilterRegistrationBean<>(filtro);
        registro.addUrlPatterns("/uploads/*");
        return registro;
    }
}
