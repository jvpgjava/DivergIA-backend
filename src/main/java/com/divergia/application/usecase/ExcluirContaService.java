package com.divergia.application.usecase;

import com.divergia.application.port.in.ExcluirContaUseCase;
import com.divergia.application.port.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExcluirContaService implements ExcluirContaUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public ExcluirContaService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void excluir(UUID usuarioId) {
        usuarioRepository.excluir(usuarioId);
    }
}
