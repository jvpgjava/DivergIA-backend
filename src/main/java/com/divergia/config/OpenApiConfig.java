package com.divergia.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI divergiaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("DivergIA — API")
                        .description("Compara um texto original com uma versão editada por IA generativa, "
                                + "identifica se sentido, posição ou intensidade foram alterados além de uma "
                                + "correção de estilo, e sugere uma reescrita alternativa fiel ao sentido original.")
                        .version("v0"));
    }
}
