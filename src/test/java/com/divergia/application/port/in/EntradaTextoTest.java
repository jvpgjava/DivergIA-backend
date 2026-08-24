package com.divergia.application.port.in;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntradaTextoTest {

    @Test
    void deveAceitarApenasTexto() {
        EntradaTexto entrada = EntradaTexto.deTexto("um texto qualquer");

        assertThat(entrada.ehArquivo()).isFalse();
        assertThat(entrada.texto()).isEqualTo("um texto qualquer");
    }

    @Test
    void deveAceitarApenasArquivo() {
        EntradaTexto entrada = EntradaTexto.deArquivo(new byte[] {1, 2, 3}, "documento.pdf");

        assertThat(entrada.ehArquivo()).isTrue();
        assertThat(entrada.nomeArquivo()).isEqualTo("documento.pdf");
    }

    @Test
    void deveRejeitarQuandoNemTextoNemArquivoForemInformados() {
        assertThatThrownBy(() -> new EntradaTexto(null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EntradaTexto("   ", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarQuandoTextoEArquivoForemInformadosAoMesmoTempo() {
        assertThatThrownBy(() -> new EntradaTexto("texto", new byte[] {1}, "arquivo.pdf"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
