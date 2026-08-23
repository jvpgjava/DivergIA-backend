package com.divergia.application.usecase;

import com.divergia.application.port.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExcluirContaServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Test
    void deveExcluirUsuarioPorId() {
        ExcluirContaService service = new ExcluirContaService(usuarioRepository);
        UUID usuarioId = UUID.randomUUID();

        service.excluir(usuarioId);

        verify(usuarioRepository).excluir(usuarioId);
    }
}
