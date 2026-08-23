package com.divergia.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de ponta a ponta da superfície REST exposta até a Fase 0: health
 * check público, documentação OpenAPI/Swagger pública, e postura de
 * "negar por padrão" da Spring Security para qualquer rota não liberada
 * explicitamente. Sobe o contexto Spring completo (DispatcherServlet, filtros
 * de segurança reais) via MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RestEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveResponderHealthCheckPublicoComStatusUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"UP\"")))
                .andExpect(content().string(containsString("\"db\"")));
    }

    @Test
    void deveExporDocumentacaoOpenApiPublicamente() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"openapi\"")))
                .andExpect(content().string(containsString("DivergIA")));
    }

    @Test
    void deveExporSwaggerUiPublicamente() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("swagger-ui")));
    }

    @Test
    void deveRejeitarRequisicaoNaoAutenticadaEmRotaNaoLiberada() throws Exception {
        mockMvc.perform(get("/api/qualquer-coisa"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 401 && status != 403) {
                        throw new AssertionError("Esperado 401 ou 403, recebido " + status);
                    }
                });
    }
}
