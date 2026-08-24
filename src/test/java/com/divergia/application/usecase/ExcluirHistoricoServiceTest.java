package com.divergia.application.usecase;

import com.divergia.application.port.out.AnaliseRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExcluirHistoricoServiceTest {

    @Mock
    private AnaliseRepositoryPort analiseRepository;

    @Test
    void deveExcluirTodasAsAnalisesDoUsuario() {
        ExcluirHistoricoService service = new ExcluirHistoricoService(analiseRepository);
        UUID usuarioId = UUID.randomUUID();

        service.excluirTudo(usuarioId);

        verify(analiseRepository).excluirTodasPorUsuarioId(usuarioId);
    }
}
