package com.divergia.application.usecase;

import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.LlmPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.TrechoDeriva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SugerirReescritaServiceTest {

    @Mock
    private TrechoDerivaRepositoryPort trechoDerivaRepository;

    @Mock
    private AnaliseRepositoryPort analiseRepository;

    @Mock
    private VectorStorePort vectorStorePort;

    @Mock
    private LlmPort llmPort;

    private SugerirReescritaService service;

    @BeforeEach
    void setUp() {
        service = new SugerirReescritaService(
                trechoDerivaRepository, analiseRepository, vectorStorePort, llmPort, 5);
    }

    @Test
    void deveSugerirReescritaParaTrechoDoProprioUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID trechoId = UUID.randomUUID();
        UUID analiseId = UUID.randomUUID();

        TrechoDeriva trecho = new TrechoDeriva(
                trechoId, analiseId, "o prazo é de dois anos", "o prazo é rápido",
                TipoDesvio.SENTIDO, "prazo específico virou vago", 0.8, false);
        Analise analise = new Analise(analiseId, usuarioId, "original", "editado", true, Instant.now());

        given(trechoDerivaRepository.buscarPorId(trechoId)).willReturn(Optional.of(trecho));
        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.of(analise));
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.sugerirReescrita(
                eq("o prazo é de dois anos"), eq("o prazo é rápido"), eq(TipoDesvio.SENTIDO), anyString(), any()))
                .willReturn("o prazo é de dois anos, mas pode variar");

        String sugestao = service.sugerir(usuarioId, trechoId);

        assertThat(sugestao).isEqualTo("o prazo é de dois anos, mas pode variar");
    }

    @Test
    void deveLancarExcecaoQuandoTrechoNaoExiste() {
        UUID usuarioId = UUID.randomUUID();
        UUID trechoId = UUID.randomUUID();
        given(trechoDerivaRepository.buscarPorId(trechoId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.sugerir(usuarioId, trechoId))
                .isInstanceOf(TrechoDerivaNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoTrechoPertenceAOutroUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID outroUsuarioId = UUID.randomUUID();
        UUID trechoId = UUID.randomUUID();
        UUID analiseId = UUID.randomUUID();

        TrechoDeriva trecho = new TrechoDeriva(
                trechoId, analiseId, "original", "editado", TipoDesvio.POSICAO, "explicacao", 0.5, false);
        Analise analiseDeOutroUsuario = new Analise(
                analiseId, outroUsuarioId, "original", "editado", true, Instant.now());

        given(trechoDerivaRepository.buscarPorId(trechoId)).willReturn(Optional.of(trecho));
        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.of(analiseDeOutroUsuario));

        assertThatThrownBy(() -> service.sugerir(usuarioId, trechoId))
                .isInstanceOf(AcessoNaoAutorizadoException.class);
    }
}
