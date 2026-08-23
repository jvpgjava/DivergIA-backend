package com.divergia.adapter.out.vectorstore;

import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.OrigemExemplo;
import com.divergia.domain.model.TipoDesvio;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integração do adapter de vetor contra um servidor HTTP local que imita o
 * endpoint {@code embedContent} do Gemini — nenhuma chamada bate na API real
 * durante o teste/CI. A busca por similaridade roda contra o Postgres/pgvector
 * real (ver seção de Infraestrutura do README).
 */
@SpringBootTest
@Transactional
class GeminiVectorStoreAdapterTest {

    private static HttpServer servidorMock;

    @BeforeAll
    static void subirServidorMock() throws IOException {
        servidorMock = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidorMock.createContext("/v1beta/models/gemini-embedding-001:embedContent", exchange -> {
            String valores = IntStream.range(0, 768)
                    .mapToObj(i -> String.valueOf(new Random(i).nextFloat() * 0.1f))
                    .collect(Collectors.joining(","));
            String corpo = "{\"embedding\":{\"values\":[" + valores + "]}}";
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
        registry.add("divergia.embedding.base-url",
                () -> "http://localhost:" + servidorMock.getAddress().getPort() + "/v1beta");
        registry.add("divergia.embedding.api-key", () -> "chave-de-teste");
    }

    @Autowired
    private GeminiVectorStoreAdapter adapter;

    @Test
    void deveSalvarExemploComEmbeddingEEncontraLoNaBuscaPorSimilaridade() {
        ExemploRag salvo = adapter.salvar(
                "a decisão foi adiada", "a decisão foi cancelada",
                TipoDesvio.SENTIDO, OrigemExemplo.PROMOVIDO_DE_ANALISE);

        assertThat(salvo.id()).isNotNull();
        assertThat(salvo.embedding()).hasSize(768);

        List<ExemploRag> encontrados = adapter.buscarSimilares("a decisão foi adiada", 5);

        assertThat(encontrados).extracting(ExemploRag::id).contains(salvo.id());
    }
}
