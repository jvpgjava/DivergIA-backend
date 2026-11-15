package com.divergia.application.usecase;

import com.divergia.application.port.in.EntradaAnalise;
import com.divergia.application.port.in.EntradaTexto;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.ExtracaoDocumentoPort;
import com.divergia.application.port.out.LlmPort;
import com.divergia.application.port.out.MetricasGlobaisPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.AvaliacaoDoModelo;
import com.divergia.domain.model.CampoEntrada;
import com.divergia.domain.model.MotivoEntradaInvalida;
import com.divergia.domain.model.ResultadoAnalise;
import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.TrechoDeriva;
import com.divergia.domain.model.TriagemDaEntrada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionOperations;

import java.util.List;
import java.util.UUID;

import static com.divergia.testutil.Modelos.modelo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AnalisarTextoServiceTest {

    private static final String ORIGINAL = "O produto é bom e atende ao que promete.";
    private static final String EDITADO = "O produto é excepcional e supera tudo o que promete.";

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

    @Mock
    private MetricasGlobaisPort metricasGlobais;

    private AnalisarTextoService service;

    @BeforeEach
    void setUp() {
        service = new AnalisarTextoService(
                extracaoDocumentoPort, new BuscadorDeExemplosRag(vectorStorePort, 5), llmPort, analiseRepository,
                trechoDerivaRepository, metricasGlobais, TransactionOperations.withoutTransaction(), 10000, 0.7);
    }

    private static AvaliacaoDeDeriva derivaValida() {
        return new AvaliacaoDeDeriva(
                TipoDesvio.INTENSIDADE, "O produto é bom", "O produto é excepcional",
                "A avaliação foi ampliada de boa para excepcional, além de uma correção de estilo.", 0.6);
    }

    private static EntradaAnalise entrada(String original, String editado, boolean manterHistorico) {
        return new EntradaAnalise(
                UUID.randomUUID(), EntradaTexto.deTexto(original), EntradaTexto.deTexto(editado), manterHistorico);
    }

    private void persistenciaEcoando() {
        given(analiseRepository.salvar(any(Analise.class))).willAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deveAnalisarParDeTextosColadosEPersistirComHistorico() {
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(modelo(derivaValida()));
        persistenciaEcoando();
        given(trechoDerivaRepository.salvar(any(TrechoDeriva.class))).willAnswer(inv -> inv.getArgument(0));

        ResultadoAnalise resultado = service.analisar(entrada(ORIGINAL, EDITADO, true));

        assertThat(resultado.analise().textoOriginal()).isEqualTo(ORIGINAL);
        assertThat(resultado.analise().textoEditado()).isEqualTo(EDITADO);
        assertThat(resultado.trechosDeDeriva()).hasSize(1);
        assertThat(resultado.trechosDeDeriva().get(0).tipoDesvio()).isEqualTo(TipoDesvio.INTENSIDADE);
        verify(trechoDerivaRepository).salvar(any(TrechoDeriva.class));
        verify(extracaoDocumentoPort, never()).extrairTexto(any(), anyString());
    }

    @Test
    void deveContabilizarNoPainelGlobalAnaliseEDivergenciasInclusiveSemHistorico() {
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(modelo(derivaValida()));
        persistenciaEcoando();

        service.analisar(entrada(ORIGINAL, EDITADO, false));

        verify(metricasGlobais).registrarAnalise(any(), eq(List.of(TipoDesvio.INTENSIDADE)));
    }

    @Test
    void deveContabilizarAnaliseSemDivergenciasQuandoOsTextosSaoIguais() {
        persistenciaEcoando();

        service.analisar(entrada(ORIGINAL, ORIGINAL, true));

        verify(metricasGlobais).registrarAnalise(any(), eq(List.of()));
    }

    @Test
    void naoDeveContabilizarQuandoAEntradaForRecusada() {
        assertThatThrownBy(() -> service.analisar(entrada(ORIGINAL, "01101111 01101100 01100001 00100000 01101101 01110101 01101110 01100100 01101111", true)))
                .isInstanceOf(EntradaInvalidaException.class);

        verifyNoInteractions(metricasGlobais);
    }

    @Test
    void deveExtrairTextoDoArquivoQuandoOriginalForArquivo() {
        byte[] arquivo = "conteudo do pdf".getBytes();
        EntradaAnalise entrada = new EntradaAnalise(
                UUID.randomUUID(), EntradaTexto.deArquivo(arquivo, "original.pdf"), EntradaTexto.deTexto(EDITADO), true);
        given(extracaoDocumentoPort.extrairTexto(arquivo, "original.pdf")).willReturn(ORIGINAL);
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(modelo());
        persistenciaEcoando();

        ResultadoAnalise resultado = service.analisar(entrada);

        assertThat(resultado.analise().textoOriginal()).isEqualTo(ORIGINAL);
        verify(extracaoDocumentoPort).extrairTexto(arquivo, "original.pdf");
    }

    @Test
    void naoDevePersistirTrechosQuandoNaoHaConsentimentoDeHistoricoMasDeveDevolveLosNoResultado() {
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(modelo(derivaValida()));
        persistenciaEcoando();

        ResultadoAnalise resultado = service.analisar(entrada(ORIGINAL, EDITADO, false));

        assertThat(resultado.analise().textoOriginal()).isNull();
        assertThat(resultado.analise().textoEditado()).isNull();
        assertThat(resultado.trechosDeDeriva()).hasSize(1);
        assertThat(resultado.trechosDeDeriva().get(0).trechoOriginal()).isEqualTo("O produto é bom");
        verify(trechoDerivaRepository, never()).salvar(any());
    }

    @Test
    void deveConsultarRagComOsTextosOriginalEEditado() {
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(modelo());
        persistenciaEcoando();

        service.analisar(entrada(ORIGINAL, EDITADO, true));

        ArgumentCaptor<String> consulta = ArgumentCaptor.forClass(String.class);
        verify(vectorStorePort).buscarSimilares(consulta.capture(), eq(5));
        assertThat(consulta.getValue()).contains(ORIGINAL).contains(EDITADO);
    }

    @Test
    void deveSeguirSemExemplosQuandoORagFalhar() {
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willThrow(new RuntimeException("gemini fora"));
        given(llmPort.avaliarDerivas(anyString(), anyString(), eq(List.of()))).willReturn(modelo(derivaValida()));
        persistenciaEcoando();
        given(trechoDerivaRepository.salvar(any(TrechoDeriva.class))).willAnswer(inv -> inv.getArgument(0));

        assertThat(service.analisar(entrada(ORIGINAL, EDITADO, true)).trechosDeDeriva()).hasSize(1);
    }

    @Test
    void deveLimparCaracteresInvisiveisAntesDeEnviarAoModelo() {
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(modelo());
        persistenciaEcoando();

        service.analisar(entrada("O produto​ é bom‍ e atende ao que promete.", EDITADO, true));

        verify(llmPort).avaliarDerivas(eq(ORIGINAL), eq(EDITADO), any());
    }

    @Test
    void deveRecusarBinarioSemChamarOModelo() {
        String binario = "01101111 01101100 01100001 00100000 01101101 01110101 01101110 01100100 01101111";

        assertThatThrownBy(() -> service.analisar(entrada(ORIGINAL, binario, true)))
                .isInstanceOfSatisfying(EntradaInvalidaException.class, e -> {
                    assertThat(e.problema().motivo()).isEqualTo(MotivoEntradaInvalida.CONTEUDO_CODIFICADO);
                    assertThat(e.problema().campo()).isEqualTo(CampoEntrada.EDITADO);
                });
        verifyNoInteractions(llmPort, analiseRepository);
    }

    @Test
    void deveRecusarIdiomasDiferentesSemChamarOModelo() {
        String original = "Eu acho que essa proposta não é boa para a nossa cidade e para os moradores.";
        String editado = "I think that this proposal is not good for our city and for the people who live in it.";

        assertThatThrownBy(() -> service.analisar(entrada(original, editado, true)))
                .isInstanceOfSatisfying(EntradaInvalidaException.class, e -> {
                    assertThat(e.problema().motivo()).isEqualTo(MotivoEntradaInvalida.IDIOMAS_DIFERENTES);
                    assertThat(e.getMessage()).contains("português").contains("inglês");
                });
        verifyNoInteractions(llmPort);
    }

    @Test
    void deveRecusarArquivoSemTextoLegivel() {
        byte[] arquivo = "pdf escaneado".getBytes();
        EntradaAnalise entrada = new EntradaAnalise(
                UUID.randomUUID(), EntradaTexto.deArquivo(arquivo, "scan.pdf"), EntradaTexto.deTexto(EDITADO), true);
        given(extracaoDocumentoPort.extrairTexto(arquivo, "scan.pdf")).willReturn("   \n  ");

        assertThatThrownBy(() -> service.analisar(entrada))
                .isInstanceOfSatisfying(EntradaInvalidaException.class, e ->
                        assertThat(e.problema().motivo()).isEqualTo(MotivoEntradaInvalida.ARQUIVO_SEM_TEXTO));
        verifyNoInteractions(llmPort);
    }

    @Test
    void textosIdenticosNaoChamamOModeloENaoTemDeriva() {
        persistenciaEcoando();

        ResultadoAnalise resultado = service.analisar(entrada(ORIGINAL, "  " + ORIGINAL + "\n", true));

        assertThat(resultado.trechosDeDeriva()).isEmpty();
        verifyNoInteractions(llmPort, vectorStorePort);
    }

    @Test
    void deveRecusarQuandoATriagemDoModeloDizQueOsTextosNaoTemRelacaoComConfiancaAlta() {
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(new AvaliacaoDoModelo(
                new TriagemDaEntrada(MotivoEntradaInvalida.SEM_RELACAO, CampoEntrada.AMBOS, "pt", "pt", 0.9),
                List.of()));

        assertThatThrownBy(() -> service.analisar(entrada(ORIGINAL, EDITADO, true)))
                .isInstanceOfSatisfying(EntradaInvalidaException.class, e ->
                        assertThat(e.problema().motivo()).isEqualTo(MotivoEntradaInvalida.SEM_RELACAO));
        verifyNoInteractions(analiseRepository);
    }

    @Test
    void triagemDoModeloComConfiancaBaixaNaoRecusa() {
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(new AvaliacaoDoModelo(
                new TriagemDaEntrada(MotivoEntradaInvalida.SEM_RELACAO, CampoEntrada.AMBOS, "pt", "pt", 0.4),
                List.of()));
        persistenciaEcoando();

        assertThat(service.analisar(entrada(ORIGINAL, EDITADO, true)).trechosDeDeriva()).isEmpty();
    }

    @Test
    void deveDescartarDerivaComCitacaoInventadaEManterAVerdadeira() {
        AvaliacaoDeDeriva inventada = new AvaliacaoDeDeriva(
                TipoDesvio.SENTIDO, "o produto é barato", "o produto é caro",
                "O preço mudou de barato para caro, o que altera a ideia.", 0.8);
        given(vectorStorePort.buscarSimilares(anyString(), anyInt())).willReturn(List.of());
        given(llmPort.avaliarDerivas(anyString(), anyString(), any())).willReturn(modelo(inventada, derivaValida()));
        persistenciaEcoando();
        given(trechoDerivaRepository.salvar(any(TrechoDeriva.class))).willAnswer(inv -> inv.getArgument(0));

        ResultadoAnalise resultado = service.analisar(entrada(ORIGINAL, EDITADO, true));

        assertThat(resultado.trechosDeDeriva()).extracting(TrechoDeriva::trechoEditado)
                .containsExactly("O produto é excepcional");
    }
}
