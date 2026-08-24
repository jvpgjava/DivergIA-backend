package com.divergia.application.usecase;

import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.domain.model.Analise;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ListarHistoricoServiceTest {

    @Mock
    private AnaliseRepositoryPort analiseRepository;

    @Test
    void deveListarAnalisesDoUsuarioOrdenadasPorDataDecrescente() {
        UUID usuarioId = UUID.randomUUID();
        Instant agora = Instant.now();
        Analise maisAntiga = new Analise(UUID.randomUUID(), usuarioId, "a", "b", true, agora.minusSeconds(3600));
        Analise maisRecente = new Analise(UUID.randomUUID(), usuarioId, "c", "d", true, agora);

        given(analiseRepository.buscarPorUsuarioId(usuarioId)).willReturn(List.of(maisAntiga, maisRecente));

        ListarHistoricoService service = new ListarHistoricoService(analiseRepository);
        List<Analise> resultado = service.listar(usuarioId);

        assertThat(resultado).containsExactly(maisRecente, maisAntiga);
    }
}
