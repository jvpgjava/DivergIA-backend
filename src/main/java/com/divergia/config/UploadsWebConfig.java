package com.divergia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
}
