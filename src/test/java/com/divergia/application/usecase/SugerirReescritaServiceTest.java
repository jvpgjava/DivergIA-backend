package com.divergia.application.usecase;

import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.LlmException;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SugerirReescritaServiceTest {

    private static final String TRECHO_ORIGINAL = "o prazo é de dois anos";
    private static final String TRECHO_EDITADO = "o prazo é rápido";

    @Mock
    private TrechoDerivaRepositoryPort trechoDerivaRepository;

    @Mock
    private AnaliseRepositoryPort analiseRepository;

    @Mock
    private VectorStorePort vectorStorePort;

    @Mock
    private LlmPort llmPort;

    private SugerirReescritaService service;
    private UUID usuarioId;
    private UUID trechoId;

    @BeforeEach
    void setUp() {
        service = new SugerirReescritaService(
                trechoDerivaRepository, analiseRepository, new BuscadorDeExemplosRag(vectorStorePort, 5), llmPort, true);
        usuarioId = UUID.randomUUID();
        trechoId = UUID.randomUUID();
    }

    private void trechoDoUsuario() {
        UUID analiseId = UUID.randomUUID();
        TrechoDeriva trecho = new TrechoDeriva(
                trechoId, analiseId, TRECHO_ORIGINAL, TRECHO_EDITADO,
                TipoDesvio.SENTIDO, "prazo específico virou vago", 0.8, false);
        given(trechoDerivaRepository.buscarPorId(trechoId)).willReturn(Optional.of(trecho));
        given(analiseRepository.buscarPorId(analiseId))
                .willReturn(Optional.of(new Analise(analiseId, usuarioId, "original", "editado", true, Instant.now())));
    }

    private void modeloSugere(List<String> sugestoes) {
        given(llmPort.sugerirReescrita(
                eq(TRECHO_ORIGINAL), eq(TRECHO_EDITADO), eq(TipoDesvio.SENTIDO), anyString(), any()))
                .willReturn(sugestoes);
    }

    @Test
    void deveDevolverAsSugestoesQueOJuizAprova() {
        trechoDoUsuario();
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        modeloSugere(List.of(
                "o prazo definido é de dois anos",
                "o prazo é de dois anos, talvez menos",
                "dois anos é o prazo"));
        given(llmPort.verificarFidelidade(eq(TRECHO_ORIGINAL), anyList())).willReturn(List.of(true, false, true));

        List<String> sugestoes = service.sugerir(usuarioId, trechoId);

        assertThat(sugestoes).containsExactly("o prazo definido é de dois anos", "dois anos é o prazo");
    }

    @Test
    void comJuizIndisponivelDeveSeguirSoComAGuardaDeterministica() {
        trechoDoUsuario();
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        modeloSugere(List.of("o prazo definido é de dois anos", "dois anos é o prazo"));
        given(llmPort.verificarFidelidade(anyString(), anyList())).willReturn(List.of());

        assertThat(service.sugerir(usuarioId, trechoId)).hasSize(2);
    }

    @Test
    void deveDescartarCopiaDoTrechoEditadoEMetacomentario() {
        trechoDoUsuario();
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        modeloSugere(List.of(
                "Aqui estão as alternativas: o prazo é de dois anos",
                "o prazo é rápido",
                "1. \"o prazo definido é de dois anos\""));
        given(llmPort.verificarFidelidade(anyString(), anyList())).willReturn(List.of(true));

        assertThat(service.sugerir(usuarioId, trechoId)).containsExactly("o prazo definido é de dois anos");
    }

    @Test
    void quandoNadaPassaDeveTentarDeNovoEDepoisRecusar() {
        trechoDoUsuario();
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        modeloSugere(List.of("o prazo é de dois anos, talvez menos"));
        given(llmPort.verificarFidelidade(anyString(), anyList())).willReturn(List.of(false));

        assertThatThrownBy(() -> service.sugerir(usuarioId, trechoId))
                .isInstanceOf(SugestaoIndisponivelException.class);
        verify(llmPort, times(SugerirReescritaService.TENTATIVAS))
                .sugerirReescrita(anyString(), anyString(), any(), anyString(), any());
    }

    @Test
    void falhaDoModeloNasDuasTentativasDevePropagarLlmException() {
        trechoDoUsuario();
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.sugerirReescrita(anyString(), anyString(), any(), anyString(), any()))
                .willThrow(new LlmException("fora do formato"));

        assertThatThrownBy(() -> service.sugerir(usuarioId, trechoId)).isInstanceOf(LlmException.class);
    }

    @Test
    void deveSeguirSemExemplosQuandoORagFalhar() {
        trechoDoUsuario();
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willThrow(new RuntimeException("gemini fora"));
        modeloSugere(List.of("o prazo definido é de dois anos"));
        given(llmPort.verificarFidelidade(anyString(), anyList())).willReturn(List.of(true));

        assertThat(service.sugerir(usuarioId, trechoId)).hasSize(1);
    }

    @Test
    void comJuizDesligadoNaoChamaOJuiz() {
        service = new SugerirReescritaService(
                trechoDerivaRepository, analiseRepository, new BuscadorDeExemplosRag(vectorStorePort, 5), llmPort, false);
        trechoDoUsuario();
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        modeloSugere(List.of("o prazo definido é de dois anos"));

        assertThat(service.sugerir(usuarioId, trechoId)).hasSize(1);
        verify(llmPort, never()).verificarFidelidade(anyString(), anyList());
    }

    @Test
    void deveLancarExcecaoQuandoTrechoNaoExiste() {
        given(trechoDerivaRepository.buscarPorId(trechoId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.sugerir(usuarioId, trechoId))
                .isInstanceOf(TrechoDerivaNaoEncontradoException.class);
    }

    @Test
    void deveLancarExcecaoQuandoTrechoPertenceAOutroUsuario() {
        UUID analiseId = UUID.randomUUID();
        TrechoDeriva trecho = new TrechoDeriva(
                trechoId, analiseId, "original", "editado", TipoDesvio.POSICAO, "explicacao", 0.5, false);
        given(trechoDerivaRepository.buscarPorId(trechoId)).willReturn(Optional.of(trecho));
        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.of(
                new Analise(analiseId, UUID.randomUUID(), "original", "editado", true, Instant.now())));

        assertThatThrownBy(() -> service.sugerir(usuarioId, trechoId))
                .isInstanceOf(AcessoNaoAutorizadoException.class);
    }
}
