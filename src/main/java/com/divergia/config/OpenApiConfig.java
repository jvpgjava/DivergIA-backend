package com.divergia.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_BEARER = "bearer-jwt";

    @Bean
    public OpenAPI divergiaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("DivergIA — API")
                        .description("Compara um texto original com uma versão editada por IA generativa, "
                                + "identifica se sentido, posição ou intensidade foram alterados além de uma "
                                + "correção de estilo, e sugere uma reescrita alternativa fiel ao sentido original.")
                        .version("v0"))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_BEARER, new SecurityScheme()
                                .name(ESQUEMA_BEARER)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token obtido em POST /api/auth/login")));
    }
}
