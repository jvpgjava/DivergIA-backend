package com.divergia.adapter.out.storage;

import com.divergia.application.port.out.FotoPerfilPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class FotoPerfilDiscoAdapter implements FotoPerfilPort {

    private final Path diretorio;
    private final String baseUrl;

    public FotoPerfilDiscoAdapter(
            @Value("${divergia.uploads.diretorio}") String diretorio,
            @Value("${divergia.uploads.base-url}") String baseUrl) {
        this.diretorio = Path.of(diretorio);
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public String salvar(UUID usuarioId, byte[] conteudo, String extensao) {
        try {
            Files.createDirectories(diretorio);
            String nomeArquivo = usuarioId + "-" + System.currentTimeMillis() + "." + extensao;
            Files.write(diretorio.resolve(nomeArquivo), conteudo);
            return baseUrl + "/" + nomeArquivo;
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível salvar a foto de perfil", e);
        }
    }
}
