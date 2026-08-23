package com.divergia.adapter.out.extraction;

import com.divergia.application.port.out.ExtracaoDocumentoException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integração do adapter contra um servidor HTTP local que imita o
 * microsserviço de extração — nenhuma chamada real acontece no teste/CI.
 */
class DoclingExtracaoDocumentoAdapterTest {

    @Test
    void deveExtrairTextoViaServico() throws IOException {
        HttpServer servidorMock = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        try {
            servidorMock.createContext("/extrair", exchange -> {
                String corpo = "{\"texto\":\"conteudo extraido do documento de teste\"}";
                byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                try (var os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            });
            servidorMock.start();

            DoclingExtracaoDocumentoAdapter adapter = new DoclingExtracaoDocumentoAdapter(
                    new ExtracaoDocumentoProperties("http://localhost:" + servidorMock.getAddress().getPort(), 5));

            String texto = adapter.extrairTexto("conteudo qualquer".getBytes(StandardCharsets.UTF_8), "teste.pdf");

            assertThat(texto).isEqualTo("conteudo extraido do documento de teste");
        } finally {
            servidorMock.stop(0);
        }
    }

    @Test
    void deveLancarExcecaoDeDominioQuandoServicoRespondeComErro() throws IOException {
        HttpServer servidorMock = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        try {
            servidorMock.createContext("/extrair", exchange -> {
                byte[] bytes = "{\"detail\":\"documento inválido\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(422, bytes.length);
                try (var os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            });
            servidorMock.start();

            DoclingExtracaoDocumentoAdapter adapter = new DoclingExtracaoDocumentoAdapter(
                    new ExtracaoDocumentoProperties("http://localhost:" + servidorMock.getAddress().getPort(), 5));

            assertThatThrownBy(() -> adapter.extrairTexto("conteudo".getBytes(StandardCharsets.UTF_8), "ruim.bin"))
                    .isInstanceOf(ExtracaoDocumentoException.class);
        } finally {
            servidorMock.stop(0);
        }
    }

    @Test
    void deveLancarExcecaoDeDominioQuandoServicoEstaIndisponivel() {
        // porta em localhost sem nenhum servidor escutando
        DoclingExtracaoDocumentoAdapter adapter = new DoclingExtracaoDocumentoAdapter(
                new ExtracaoDocumentoProperties("http://localhost:1", 2));

        assertThatThrownBy(() -> adapter.extrairTexto("conteudo".getBytes(StandardCharsets.UTF_8), "teste.pdf"))
                .isInstanceOf(ExtracaoDocumentoException.class);
    }
}
