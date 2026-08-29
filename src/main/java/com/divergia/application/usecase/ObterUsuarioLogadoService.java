package com.divergia.application.usecase;

import com.divergia.application.port.in.ObterUsuarioLogadoUseCase;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import com.divergia.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ObterUsuarioLogadoService implements ObterUsuarioLogadoUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public ObterUsuarioLogadoService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario obter(UUID usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
    }
}
