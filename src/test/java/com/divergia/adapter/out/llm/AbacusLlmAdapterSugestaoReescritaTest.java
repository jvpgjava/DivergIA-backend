package com.divergia.adapter.out.llm;

import com.divergia.domain.model.TipoDesvio;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integração do método de sugestão de reescrita contra um servidor HTTP
 * local que imita o formato de resposta OpenAI-compatible — nenhuma chamada
 * bate na API real durante o teste/CI.
 */
@SpringBootTest
class AbacusLlmAdapterSugestaoReescritaTest {

    private static HttpServer servidorMock;

    @BeforeAll
    static void subirServidorMock() throws IOException {
        servidorMock = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidorMock.createContext("/v1/chat/completions", exchange -> {
            String corpo = "{"
                    + "\"id\":\"chatcmpl-teste\","
                    + "\"object\":\"chat.completion\","
                    + "\"created\":1710000000,"
                    + "\"model\":\"claude-sonnet-5\","
                    + "\"choices\":[{"
                    + "\"index\":0,"
                    + "\"message\":{\"role\":\"assistant\",\"content\":"
                    + "\"\\\"o prazo é de dois anos, sujeito a ajustes\\\"\"},"
                    + "\"finish_reason\":\"stop\"}]}";
            byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        servidorMock.start();
    }

    @AfterAll
    static void pararServidorMock() {
        servidorMock.stop(0);
    }

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) {
        registry.add(
                "divergia.llm.base-url", () -> "http://localhost:" + servidorMock.getAddress().getPort() + "/v1");
        registry.add("divergia.llm.api-key", () -> "chave-de-teste");
    }

    @Autowired
    private AbacusLlmAdapter adapter;

    @Test
    void deveSugerirReescritaERemoverAspasEnvolventesDaResposta() {
        String sugestao = adapter.sugerirReescrita(
                "o prazo é de dois anos", "o prazo é rápido", TipoDesvio.SENTIDO,
                "prazo específico virou vago", List.of());

        assertThat(sugestao).isEqualTo("o prazo é de dois anos, sujeito a ajustes");
    }
}
