package com.divergia.adapter.out.llm;

import com.divergia.domain.model.AvaliacaoDeDeriva;
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
 * Cobre a resiliência do parser a respostas malformadas do LLM — comum
 * quando o texto de entrada é degenerado (só números, dígitos binários,
 * etc.) e o modelo "inventa" uma deriva com campos fora do esperado, em vez
 * de simplesmente respeitar o array vazio pedido no prompt.
 */
@SpringBootTest
class AbacusLlmAdapterResilienciaTest {

    private static HttpServer servidorMock;

    @BeforeAll
    static void subirServidorMock() throws IOException {
        servidorMock = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidorMock.createContext("/v1/chat/completions", exchange -> {
            // Três itens: intensidade fora de 0-1 (escala 0-100, deve ser
            // reescalada), tipoDesvio inexistente (deve ser ignorado) e um
            // item válido (deve passar sem alteração).
            String conteudo = "[{\\\"tipoDesvio\\\":\\\"INTENSIDADE\\\",\\\"trechoOriginal\\\":\\\"1010\\\","
                    + "\\\"trechoEditado\\\":\\\"0101\\\",\\\"explicacao\\\":\\\"desvio numerico\\\","
                    + "\\\"intensidade\\\":90},"
                    + "{\\\"tipoDesvio\\\":\\\"INEXISTENTE\\\",\\\"trechoOriginal\\\":\\\"a\\\","
                    + "\\\"trechoEditado\\\":\\\"b\\\",\\\"explicacao\\\":\\\"tipo invalido\\\","
                    + "\\\"intensidade\\\":0.5},"
                    + "{\\\"tipoDesvio\\\":\\\"SENTIDO\\\",\\\"trechoOriginal\\\":\\\"o prazo e curto\\\","
                    + "\\\"trechoEditado\\\":\\\"o prazo e longo\\\",\\\"explicacao\\\":\\\"desvio valido\\\","
                    + "\\\"intensidade\\\":0.7}]";
            String corpo = "{"
                    + "\"id\":\"chatcmpl-teste\","
                    + "\"object\":\"chat.completion\","
                    + "\"created\":1710000000,"
                    + "\"model\":\"claude-sonnet-5\","
                    + "\"choices\":[{"
                    + "\"index\":0,"
                    + "\"message\":{\"role\":\"assistant\",\"content\":\"" + conteudo + "\"},"
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
    void deveIgnorarItemComTipoDesvioInvalidoEReescalarIntensidadeForaDeFaixa() {
        List<AvaliacaoDeDeriva> derivas = adapter.avaliarDerivas("1010", "0101", List.of());

        assertThat(derivas).hasSize(2);
        assertThat(derivas.get(0).tipoDesvio()).isEqualTo(TipoDesvio.INTENSIDADE);
        assertThat(derivas.get(0).intensidade()).isEqualTo(0.9);
        assertThat(derivas.get(1).tipoDesvio()).isEqualTo(TipoDesvio.SENTIDO);
        assertThat(derivas.get(1).intensidade()).isEqualTo(0.7);
    }
}
