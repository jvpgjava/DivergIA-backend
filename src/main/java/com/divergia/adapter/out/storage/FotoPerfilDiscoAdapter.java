package com.divergia.adapter.out.storage;

import com.divergia.application.port.out.FotoPerfilPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Component
public class FotoPerfilDiscoAdapter implements FotoPerfilPort {

    private static final Set<String> EXTENSOES = Set.of("jpg", "png", "webp");

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
            if (!EXTENSOES.contains(extensao)) {
                throw new IllegalArgumentException("Extensão de foto não permitida");
            }
            Path pasta = diretorio.toAbsolutePath().normalize();
            Files.createDirectories(pasta);
            String nomeArquivo = usuarioId + "-" + System.currentTimeMillis() + "." + extensao;
            Path destino = pasta.resolve(nomeArquivo).normalize();
            // Defesa em profundidade contra path traversal: o arquivo nunca sai da pasta de uploads.
            if (!destino.getParent().equals(pasta)) {
                throw new IllegalArgumentException("Caminho de foto inválido");
            }
            Files.write(destino, conteudo);
            return baseUrl + "/" + nomeArquivo;
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível salvar a foto de perfil", e);
        }
    }
}
