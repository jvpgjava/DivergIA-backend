package com.divergia.domain.service;

import com.divergia.domain.model.Analise;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PoliticaRetencaoDeTextoTest {

    private final PoliticaRetencaoDeTexto politica = new PoliticaRetencaoDeTexto();

    @Test
    void deveRemoverTextoBrutoQuandoUsuarioNaoConsentiuManterHistorico() {
        Analise analise = new Analise(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "texto original",
                "texto editado",
                false,
                Instant.now());

        Analise resultado = politica.aplicar(analise);

        assertThat(resultado.textoOriginal()).isNull();
        assertThat(resultado.textoEditado()).isNull();
        assertThat(resultado.manterHistorico()).isFalse();
        assertThat(resultado.id()).isEqualTo(analise.id());
    }

    @Test
    void devePreservarTextoBrutoQuandoUsuarioConsentiuManterHistorico() {
        Analise analise = new Analise(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "texto original",
                "texto editado",
                true,
                Instant.now());

        Analise resultado = politica.aplicar(analise);

        assertThat(resultado).isEqualTo(analise);
        assertThat(resultado.textoOriginal()).isEqualTo("texto original");
        assertThat(resultado.textoEditado()).isEqualTo("texto editado");
    }
}
