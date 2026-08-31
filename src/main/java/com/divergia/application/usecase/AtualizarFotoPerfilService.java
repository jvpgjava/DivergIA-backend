package com.divergia.application.usecase;

import com.divergia.application.port.in.AtualizarFotoPerfilUseCase;
import com.divergia.application.port.out.FotoPerfilPort;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AtualizarFotoPerfilService implements AtualizarFotoPerfilUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final FotoPerfilPort fotoPerfilPort;

    public AtualizarFotoPerfilService(UsuarioRepositoryPort usuarioRepository, FotoPerfilPort fotoPerfilPort) {
        this.usuarioRepository = usuarioRepository;
        this.fotoPerfilPort = fotoPerfilPort;
    }

    @Override
    public String atualizar(UUID usuarioId, byte[] conteudo, String extensao) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
        String url = fotoPerfilPort.salvar(usuarioId, conteudo, extensao);
        Usuario atualizado = new Usuario(
                usuario.id(), usuario.nome(), usuario.email(), usuario.senhaHash(), usuario.criadoEm(), url);
        usuarioRepository.salvar(atualizado);
        return url;
    }
}
