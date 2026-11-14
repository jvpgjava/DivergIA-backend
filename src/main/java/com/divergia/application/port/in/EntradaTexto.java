package com.divergia.application.port.in;

import java.util.Arrays;
import java.util.Objects;

public record EntradaTexto(String texto, byte[] arquivo, String nomeArquivo) {

    public EntradaTexto {
        boolean temTexto = texto != null && !texto.isBlank();
        boolean temArquivo = arquivo != null && arquivo.length > 0;
        if (temTexto == temArquivo) {
            throw new IllegalArgumentException(
                    "Informe exatamente um: texto colado ou arquivo (nunca os dois, nunca nenhum)");
        }
    }

    public boolean ehArquivo() {
        return arquivo != null && arquivo.length > 0;
    }

    public static EntradaTexto deTexto(String texto) {
        return new EntradaTexto(texto, null, null);
    }

    public static EntradaTexto deArquivo(byte[] arquivo, String nomeArquivo) {
        return new EntradaTexto(null, arquivo, nomeArquivo);
    }

    @Override
    // O equals gerado pelo record compararia o byte[] por referência.
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntradaTexto other)) {
            return false;
        }
        return Objects.equals(texto, other.texto)
                && Arrays.equals(arquivo, other.arquivo)
                && Objects.equals(nomeArquivo, other.nomeArquivo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto, Arrays.hashCode(arquivo), nomeArquivo);
    }
}
