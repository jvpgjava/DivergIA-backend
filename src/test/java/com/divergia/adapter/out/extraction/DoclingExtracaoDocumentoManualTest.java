package com.divergia.adapter.out.extraction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verificação manual contra o serviço de extração REAL rodando em
 * localhost:8000 (não roda em CI/`mvn test` normal). Suba o serviço
 * (extraction-service/README.md) e rode com:
 * {@code ./mvnw test -Dtest=DoclingExtracaoDocumentoManualTest -Dteste.manual=true}
 */
@EnabledIfSystemProperty(named = "teste.manual", matches = "true")
class DoclingExtracaoDocumentoManualTest {

    @Test
    void deveExtrairTextoDeUmPdfRealViaServicoRealRodandoLocalmente() throws Exception {
        DoclingExtracaoDocumentoAdapter adapter =
                new DoclingExtracaoDocumentoAdapter(new ExtracaoDocumentoProperties("http://localhost:8000", 60));

        byte[] pdf = Files.readAllBytes(
                Path.of("extraction-service/tests/fixtures/teste.pdf").toAbsolutePath().normalize());

        String texto = adapter.extrairTexto(pdf, "teste.pdf");

        assertThat(texto).contains("DivergIA").contains("extraido corretamente");
    }
}
