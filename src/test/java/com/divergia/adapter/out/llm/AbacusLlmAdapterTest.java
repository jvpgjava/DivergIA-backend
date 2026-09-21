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
 * Integração do adapter de LLM contra um servidor HTTP local que imita o
 * formato de resposta OpenAI-compatible da Abacus.AI/RouteLLM — nenhuma
 * chamada bate na API real durante o teste/CI.
 *
 * <p>O conteúdo da resposta do mock é configurável por teste (campo
 * {@code proximaResposta}) de propósito: cada {@code @DynamicPropertySource}
 * distinto cria seu próprio {@code ApplicationContext} (e, por causa dele,
 * seu próprio pool de conexões com o Postgres — ver comentário em
 * {@code application-test.yml}). Um teste extra aqui é uma linha a mais;
 * uma classe extra é um pool a mais, e na suíte inteira isso já bateu num
 * limite de conexões do Postgres em CI. Por isso todo cenário de resposta
 * do LLM deve, na medida do possível, viver nesta mesma classe/contexto em
 * vez de ganhar uma classe de teste própria.
 */
@SpringBootTest
class AbacusLlmAdapterTest {

    private static HttpServer servidorMock;
    private static volatile String proximaResposta;

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
                    + "\"message\":{\"role\":\"assistant\",\"content\":\"" + proximaResposta + "\"},"
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
    void deveAvaliarDerivasAPartirDaRespostaDoModelo() {
        proximaResposta = "[{\\\"tipoDesvio\\\":\\\"INTENSIDADE\\\",\\\"trechoOriginal\\\":\\\"o produto "
                + "é bom\\\",\\\"trechoEditado\\\":\\\"o produto é excepcional\\\",\\\"explicacao\\\":"
                + "\\\"a intensidade da afirmação foi ampliada além de uma correção de estilo\\\","
                + "\\\"intensidade\\\":0.8}]";

        List<AvaliacaoDeDeriva> derivas = adapter.avaliarDerivas(
                "o produto é bom", "o produto é excepcional", List.of());

        assertThat(derivas).hasSize(1);
        AvaliacaoDeDeriva deriva = derivas.get(0);
        assertThat(deriva.tipoDesvio()).isEqualTo(TipoDesvio.INTENSIDADE);
        assertThat(deriva.trechoOriginal()).isEqualTo("o produto é bom");
        assertThat(deriva.trechoEditado()).isEqualTo("o produto é excepcional");
        assertThat(deriva.intensidade()).isEqualTo(0.8);
    }

    /**
     * Comum quando o texto de entrada é degenerado (só números, dígitos
     * binários): o LLM às vezes "inventa" uma deriva com campos fora do
     * esperado em vez de respeitar o array vazio pedido no prompt. Cobre a
     * resiliência do parser a isso — ver {@link AbacusLlmAdapter#parsear}.
     */
    @Test
    void deveIgnorarItemComTipoDesvioInvalidoEReescalarIntensidadeForaDeFaixa() {
        // Três itens: intensidade fora de 0-1 (escala 0-100, deve ser
        // reescalada), tipoDesvio inexistente (deve ser ignorado) e um item
        // válido (deve passar sem alteração).
        proximaResposta = "[{\\\"tipoDesvio\\\":\\\"INTENSIDADE\\\",\\\"trechoOriginal\\\":\\\"1010\\\","
                + "\\\"trechoEditado\\\":\\\"0101\\\",\\\"explicacao\\\":\\\"desvio numerico\\\","
                + "\\\"intensidade\\\":90},"
                + "{\\\"tipoDesvio\\\":\\\"INEXISTENTE\\\",\\\"trechoOriginal\\\":\\\"a\\\","
                + "\\\"trechoEditado\\\":\\\"b\\\",\\\"explicacao\\\":\\\"tipo invalido\\\","
                + "\\\"intensidade\\\":0.5},"
                + "{\\\"tipoDesvio\\\":\\\"SENTIDO\\\",\\\"trechoOriginal\\\":\\\"o prazo e curto\\\","
                + "\\\"trechoEditado\\\":\\\"o prazo e longo\\\",\\\"explicacao\\\":\\\"desvio valido\\\","
                + "\\\"intensidade\\\":0.7}]";

        List<AvaliacaoDeDeriva> derivas = adapter.avaliarDerivas("1010", "0101", List.of());

        assertThat(derivas).hasSize(2);
        assertThat(derivas.get(0).tipoDesvio()).isEqualTo(TipoDesvio.INTENSIDADE);
        assertThat(derivas.get(0).intensidade()).isEqualTo(0.9);
        assertThat(derivas.get(1).tipoDesvio()).isEqualTo(TipoDesvio.SENTIDO);
        assertThat(derivas.get(1).intensidade()).isEqualTo(0.7);
    }
}
