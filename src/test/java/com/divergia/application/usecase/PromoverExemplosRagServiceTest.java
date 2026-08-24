package com.divergia.application.usecase;

import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.ConsentimentoRepositoryPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.Consentimento;
import com.divergia.domain.model.OrigemExemplo;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PromoverExemplosRagServiceTest {

    @Mock
    private TrechoDerivaRepositoryPort trechoDerivaRepository;

    @Mock
    private AnaliseRepositoryPort analiseRepository;

    @Mock
    private ConsentimentoRepositoryPort consentimentoRepository;

    @Mock
    private VectorStorePort vectorStorePort;

    private PromoverExemplosRagService service;

    @BeforeEach
    void setUp() {
        service = new PromoverExemplosRagService(
                trechoDerivaRepository, analiseRepository, consentimentoRepository, vectorStorePort);
    }

    private TrechoDeriva trechoNaoPromovido(UUID id, UUID analiseId) {
        return new TrechoDeriva(
                id, analiseId, "trecho original", "trecho editado", TipoDesvio.SENTIDO, "explicacao", 0.7, false);
    }

    @Test
    void devePromoverTrechoQuandoUsuarioConsentiuContribuirParaRag() {
        UUID trechoId = UUID.randomUUID();
        UUID analiseId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        TrechoDeriva trecho = trechoNaoPromovido(trechoId, analiseId);
        Analise analise = new Analise(analiseId, usuarioId, "original", "editado", true, Instant.now());
        Consentimento consentimento = new Consentimento(UUID.randomUUID(), usuarioId, true, true, Instant.now());

        given(trechoDerivaRepository.buscarNaoPromovidosParaRag()).willReturn(List.of(trecho));
        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.of(analise));
        given(consentimentoRepository.buscarMaisRecentePorUsuarioId(usuarioId)).willReturn(Optional.of(consentimento));

        service.executar();

        verify(vectorStorePort).salvar(
                eq("trecho original"), eq("trecho editado"), eq(TipoDesvio.SENTIDO),
                eq(OrigemExemplo.PROMOVIDO_DE_ANALISE));
        verify(trechoDerivaRepository).marcarComoPromovidoParaRag(trechoId);
    }

    @Test
    void naoDevePromoverQuandoUsuarioNaoConsentiuContribuirParaRag() {
        UUID trechoId = UUID.randomUUID();
        UUID analiseId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        TrechoDeriva trecho = trechoNaoPromovido(trechoId, analiseId);
        Analise analise = new Analise(analiseId, usuarioId, "original", "editado", true, Instant.now());
        // consentiu manter histórico, mas NÃO consentiu contribuir para o RAG
        Consentimento consentimento = new Consentimento(UUID.randomUUID(), usuarioId, true, false, Instant.now());

        given(trechoDerivaRepository.buscarNaoPromovidosParaRag()).willReturn(List.of(trecho));
        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.of(analise));
        given(consentimentoRepository.buscarMaisRecentePorUsuarioId(usuarioId)).willReturn(Optional.of(consentimento));

        service.executar();

        verify(vectorStorePort, never()).salvar(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        // mesmo não promovendo, marca como processado para não reprocessar todo dia
        verify(trechoDerivaRepository).marcarComoPromovidoParaRag(trechoId);
    }

    @Test
    void naoDevePromoverQuandoUsuarioNuncaDefiniuConsentimento() {
        UUID trechoId = UUID.randomUUID();
        UUID analiseId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        TrechoDeriva trecho = trechoNaoPromovido(trechoId, analiseId);
        Analise analise = new Analise(analiseId, usuarioId, "original", "editado", true, Instant.now());

        given(trechoDerivaRepository.buscarNaoPromovidosParaRag()).willReturn(List.of(trecho));
        given(analiseRepository.buscarPorId(analiseId)).willReturn(Optional.of(analise));
        given(consentimentoRepository.buscarMaisRecentePorUsuarioId(usuarioId)).willReturn(Optional.empty());

        service.executar();

        verify(vectorStorePort, never()).salvar(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(trechoDerivaRepository).marcarComoPromovidoParaRag(trechoId);
    }

    @Test
    void devePromoverMultiplosCandidatosIndependentemente() {
        UUID trecho1 = UUID.randomUUID();
        UUID trecho2 = UUID.randomUUID();
        UUID analiseId1 = UUID.randomUUID();
        UUID analiseId2 = UUID.randomUUID();
        UUID usuarioConsentiu = UUID.randomUUID();
        UUID usuarioNaoConsentiu = UUID.randomUUID();

        given(trechoDerivaRepository.buscarNaoPromovidosParaRag()).willReturn(List.of(
                trechoNaoPromovido(trecho1, analiseId1), trechoNaoPromovido(trecho2, analiseId2)));
        given(analiseRepository.buscarPorId(analiseId1)).willReturn(Optional.of(
                new Analise(analiseId1, usuarioConsentiu, "o", "e", true, Instant.now())));
        given(analiseRepository.buscarPorId(analiseId2)).willReturn(Optional.of(
                new Analise(analiseId2, usuarioNaoConsentiu, "o", "e", true, Instant.now())));
        given(consentimentoRepository.buscarMaisRecentePorUsuarioId(usuarioConsentiu)).willReturn(Optional.of(
                new Consentimento(UUID.randomUUID(), usuarioConsentiu, true, true, Instant.now())));
        given(consentimentoRepository.buscarMaisRecentePorUsuarioId(usuarioNaoConsentiu)).willReturn(Optional.of(
                new Consentimento(UUID.randomUUID(), usuarioNaoConsentiu, true, false, Instant.now())));

        service.executar();

        verify(vectorStorePort, org.mockito.Mockito.times(1)).salvar(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(trechoDerivaRepository).marcarComoPromovidoParaRag(trecho1);
        verify(trechoDerivaRepository).marcarComoPromovidoParaRag(trecho2);
    }
}
