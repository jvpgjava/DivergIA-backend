package com.divergia.domain.service;

import java.util.Optional;

public enum TipoDeImagem {
    JPEG("jpg"),
    PNG("png"),
    WEBP("webp");

    private final String extensao;

    TipoDeImagem(String extensao) {
        this.extensao = extensao;
    }

    public String extensao() {
        return extensao;
    }

    // Pelos bytes, nunca pelo nome ou Content-Type enviados pelo cliente: um ".png" pode ser HTML.
    public static Optional<TipoDeImagem> detectar(byte[] conteudo) {
        if (conteudo == null || conteudo.length < 12) {
            return Optional.empty();
        }
        if (byteSem(conteudo[0]) == 0xFF && byteSem(conteudo[1]) == 0xD8 && byteSem(conteudo[2]) == 0xFF) {
            return Optional.of(JPEG);
        }
        if (byteSem(conteudo[0]) == 0x89 && conteudo[1] == 'P' && conteudo[2] == 'N' && conteudo[3] == 'G'
                && conteudo[4] == '\r' && conteudo[5] == '\n') {
            return Optional.of(PNG);
        }
        if (conteudo[0] == 'R' && conteudo[1] == 'I' && conteudo[2] == 'F' && conteudo[3] == 'F'
                && conteudo[8] == 'W' && conteudo[9] == 'E' && conteudo[10] == 'B' && conteudo[11] == 'P') {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static int byteSem(byte b) {
        return b & 0xFF;
    }
}
