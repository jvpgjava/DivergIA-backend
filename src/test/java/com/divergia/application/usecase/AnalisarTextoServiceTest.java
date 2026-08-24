package com.divergia.application.usecase;

import com.divergia.application.port.in.EntradaAnalise;
import com.divergia.application.port.in.EntradaTexto;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.ExtracaoDocumentoPort;
import com.divergia.application.port.out.LlmPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.ResultadoAnalise;
import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.TrechoDeriva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalisarTextoServiceTest {

    @Mock
    private ExtracaoDocumentoPort extracaoDocumentoPort;

    @Mock
    private VectorStorePort vectorStorePort;

    @Mock
    private LlmPort llmPort;

    @Mock
    private AnaliseRepositoryPort analiseRepository;

    @Mock
    private TrechoDerivaRepositoryPort trechoDerivaRepository;

    private AnalisarTextoService service;

    @BeforeEach
    void setUp() {
        service = new AnalisarTextoService(
                extracaoDocumentoPort, vectorStorePort, llmPort, analiseRepository, trechoDerivaRepository, 5);
    }

    private AvaliacaoDeDeriva umaAvaliacao() {
        return new AvaliacaoDeDeriva(
                TipoDesvio.INTENSIDADE, "o produto é bom", "o produto é excepcional",
                "intensidade ampliada além de correção de estilo", 0.8);
    }

    @Test
    void deveAnalisarParDeTextosColadosEPersistirComHistorico() {
        UUID usuarioId = UUID.randomUUID();
        EntradaAnalise entrada = new EntradaAnalise(
                usuarioId, EntradaTexto.deTexto("original"), EntradaTexto.deTexto("editado"), true);

        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(List.of(umaAvaliacao()));
        given(analiseRepository.salvar(any(Analise.class))).willAnswer(inv -> inv.getArgument(0));
        given(trechoDerivaRepository.salvar(any(TrechoDeriva.class))).willAnswer(inv -> inv.getArgument(0));

        ResultadoAnalise resultado = service.analisar(entrada);

        assertThat(resultado.analise().usuarioId()).isEqualTo(usuarioId);
        assertThat(resultado.analise().textoOriginal()).isEqualTo("original");
        assertThat(resultado.analise().textoEditado()).isEqualTo("editado");
        assertThat(resultado.trechosDeDeriva()).hasSize(1);
        assertThat(resultado.trechosDeDeriva().get(0).tipoDesvio()).isEqualTo(TipoDesvio.INTENSIDADE);

        verify(trechoDerivaRepository).salvar(any(TrechoDeriva.class));
        verify(extracaoDocumentoPort, never()).extrairTexto(any(), anyString());
    }

    @Test
    void deveExtrairTextoDoArquivoQuandoOriginalForArquivo() {
        UUID usuarioId = UUID.randomUUID();
        byte[] arquivo = "conteudo do pdf".getBytes();
        EntradaAnalise entrada = new EntradaAnalise(
                usuarioId, EntradaTexto.deArquivo(arquivo, "original.pdf"), EntradaTexto.deTexto("editado"), true);

        given(extracaoDocumentoPort.extrairTexto(arquivo, "original.pdf")).willReturn("texto extraído do pdf");
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(List.of());
        given(analiseRepository.salvar(any(Analise.class))).willAnswer(inv -> inv.getArgument(0));

        ResultadoAnalise resultado = service.analisar(entrada);

        assertThat(resultado.analise().textoOriginal()).isEqualTo("texto extraído do pdf");
        verify(extracaoDocumentoPort).extrairTexto(arquivo, "original.pdf");
    }

    @Test
    void naoDevePersistirTrechosQuandoNaoHaConsentimentoDeHistoricoMasDeveDevolveLosNoResultado() {
        UUID usuarioId = UUID.randomUUID();
        EntradaAnalise entrada = new EntradaAnalise(
                usuarioId, EntradaTexto.deTexto("original"), EntradaTexto.deTexto("editado"), false);

        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(List.of(umaAvaliacao()));
        given(analiseRepository.salvar(any(Analise.class))).willAnswer(inv -> inv.getArgument(0));

        ResultadoAnalise resultado = service.analisar(entrada);

        assertThat(resultado.analise().textoOriginal()).isNull();
        assertThat(resultado.analise().textoEditado()).isNull();
        assertThat(resultado.trechosDeDeriva()).hasSize(1);
        assertThat(resultado.trechosDeDeriva().get(0).trechoOriginal()).isEqualTo("o produto é bom");

        verify(trechoDerivaRepository, never()).salvar(any());
    }

    @Test
    void deveConsultarRagComOsTextosOriginalEEditado() {
        UUID usuarioId = UUID.randomUUID();
        EntradaAnalise entrada = new EntradaAnalise(
                usuarioId, EntradaTexto.deTexto("texto A"), EntradaTexto.deTexto("texto B"), true);

        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(List.of());
        given(analiseRepository.salvar(any(Analise.class))).willAnswer(inv -> inv.getArgument(0));

        service.analisar(entrada);

        ArgumentCaptor<String> consultaCaptor = ArgumentCaptor.forClass(String.class);
        verify(vectorStorePort).buscarSimilares(consultaCaptor.capture(), eq(5));
        assertThat(consultaCaptor.getValue()).contains("texto A").contains("texto B");
    }
}
