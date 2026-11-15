package com.divergia.application.usecase;

import com.divergia.application.port.in.AnalisarTextoUseCase;
import com.divergia.application.port.in.EntradaAnalise;
import com.divergia.application.port.in.EntradaTexto;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.ExtracaoDocumentoPort;
import com.divergia.application.port.out.LlmPort;
import com.divergia.application.port.out.MetricasGlobaisPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.AvaliacaoDoModelo;
import com.divergia.domain.model.CampoEntrada;
import com.divergia.domain.model.MotivoEntradaInvalida;
import com.divergia.domain.model.ProblemaDeEntrada;
import com.divergia.domain.model.ResultadoAnalise;
import com.divergia.domain.model.TrechoDeriva;
import com.divergia.domain.model.TriagemDaEntrada;
import com.divergia.domain.service.DetectorDeIdioma;
import com.divergia.domain.service.GuardaDeDerivas;
import com.divergia.domain.service.NormalizadorDeTexto;
import com.divergia.domain.service.PoliticaRetencaoDeTexto;
import com.divergia.domain.service.ValidadorDeEntrada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnalisarTextoService implements AnalisarTextoUseCase {

    private static final Logger log = LoggerFactory.getLogger(AnalisarTextoService.class);

    private final ExtracaoDocumentoPort extracaoDocumentoPort;
    private final BuscadorDeExemplosRag buscadorDeExemplos;
    private final LlmPort llmPort;
    private final AnaliseRepositoryPort analiseRepository;
    private final TrechoDerivaRepositoryPort trechoDerivaRepository;
    private final MetricasGlobaisPort metricasGlobais;
    private final TransactionOperations transacao;
    private final PoliticaRetencaoDeTexto politicaRetencao = new PoliticaRetencaoDeTexto();
    private final GuardaDeDerivas guardaDeDerivas = new GuardaDeDerivas();
    private final ValidadorDeEntrada validador;
    private final double confiancaMinimaTriagem;

    public AnalisarTextoService(
            ExtracaoDocumentoPort extracaoDocumentoPort,
            BuscadorDeExemplosRag buscadorDeExemplos,
            LlmPort llmPort,
            AnaliseRepositoryPort analiseRepository,
            TrechoDerivaRepositoryPort trechoDerivaRepository,
            MetricasGlobaisPort metricasGlobais,
            TransactionOperations transacao,
            @Value("${divergia.analise.maximo-caracteres:10000}") int maximoCaracteres,
            @Value("${divergia.ia.triagem.confianca-minima:0.7}") double confiancaMinimaTriagem) {
        this.extracaoDocumentoPort = extracaoDocumentoPort;
        this.buscadorDeExemplos = buscadorDeExemplos;
        this.llmPort = llmPort;
        this.analiseRepository = analiseRepository;
        this.trechoDerivaRepository = trechoDerivaRepository;
        this.metricasGlobais = metricasGlobais;
        this.transacao = transacao;
        this.validador = new ValidadorDeEntrada(maximoCaracteres);
        this.confiancaMinimaTriagem = confiancaMinimaTriagem;
    }

    @Override
    public ResultadoAnalise analisar(EntradaAnalise entrada) {
        String textoOriginal = NormalizadorDeTexto.limpar(limitar(resolverTexto(entrada.original()), CampoEntrada.ORIGINAL));
        String textoEditado = NormalizadorDeTexto.limpar(limitar(resolverTexto(entrada.editado()), CampoEntrada.EDITADO));

        validarEntrada(entrada, textoOriginal, textoEditado);

        List<AvaliacaoDeDeriva> derivas;
        if (NormalizadorDeTexto.identicos(textoOriginal, textoEditado)) {
            derivas = List.of();
        } else {
            derivas = avaliarComGuardas(textoOriginal, textoEditado);
        }

        // A chamada ao modelo fica fora da transação para não prender uma conexão do pool por dezenas de segundos.
        return transacao.execute(status -> persistir(entrada, textoOriginal, textoEditado, derivas));
    }

    private void validarEntrada(EntradaAnalise entrada, String textoOriginal, String textoEditado) {
        validador.validarLado(textoOriginal, CampoEntrada.ORIGINAL, entrada.original().ehArquivo())
                .ifPresent(this::recusar);
        validador.validarLado(textoEditado, CampoEntrada.EDITADO, entrada.editado().ehArquivo())
                .ifPresent(this::recusar);
        validador.validarPar(textoOriginal, textoEditado).ifPresent(this::recusar);
    }

    private List<AvaliacaoDeDeriva> avaliarComGuardas(String textoOriginal, String textoEditado) {
        AvaliacaoDoModelo avaliacao = llmPort.avaliarDerivas(
                textoOriginal, textoEditado, buscadorDeExemplos.buscar(textoOriginal, textoEditado));

        TriagemDaEntrada triagem = avaliacao.triagem();
        if (!triagem.ehValida()) {
            // Com baixa confiança a análise segue: as guardas de saída ainda filtram o resultado.
            if (triagem.confianca() >= confiancaMinimaTriagem) {
                log.info("Análise recusada pela triagem do modelo: motivo={} campo={} confianca={}",
                        triagem.motivo(), triagem.campo(), triagem.confianca());
                recusar(problemaDaTriagem(triagem));
            }
            log.info("Triagem do modelo com baixa confiança ignorada: motivo={} confianca={}",
                    triagem.motivo(), triagem.confianca());
        }

        GuardaDeDerivas.Resultado resultado = guardaDeDerivas.filtrar(textoOriginal, textoEditado, avaliacao.derivas());
        if (resultado.totalDescartado() > 0 || resultado.explicacoesTrocadas() > 0) {
            log.info("Guarda de saída: {} aprovadas, descartes={}, explicacoes trocadas={}",
                    resultado.aprovadas().size(), resultado.descartes(), resultado.explicacoesTrocadas());
        }
        return resultado.aprovadas();
    }

    private ResultadoAnalise persistir(
            EntradaAnalise entrada, String textoOriginal, String textoEditado, List<AvaliacaoDeDeriva> derivas) {
        Analise analiseBruta = new Analise(
                UUID.randomUUID(), entrada.usuarioId(), textoOriginal, textoEditado,
                entrada.manterHistorico(), Instant.now());
        Analise analiseSalva = analiseRepository.salvar(politicaRetencao.aplicar(analiseBruta));

        List<TrechoDeriva> trechos = new ArrayList<>();
        for (AvaliacaoDeDeriva avaliacao : derivas) {
            TrechoDeriva trecho = new TrechoDeriva(
                    UUID.randomUUID(),
                    analiseSalva.id(),
                    avaliacao.trechoOriginal(),
                    avaliacao.trechoEditado(),
                    avaliacao.tipoDesvio(),
                    avaliacao.explicacao(),
                    avaliacao.intensidade(),
                    false);
            // Os trechos citam texto bruto, então seguem a mesma regra de retenção da análise.
            trechos.add(entrada.manterHistorico() ? trechoDerivaRepository.salvar(trecho) : trecho);
        }
        // Na mesma transação da análise. Conta a análise inclusive quando o usuário não guardou histórico
        // e depois a apaga: o contador global é só agregado, sem texto nem usuário.
        metricasGlobais.registrarAnalise(
                analiseSalva.criadoEm(), derivas.stream().map(AvaliacaoDeDeriva::tipoDesvio).toList());
        return new ResultadoAnalise(analiseSalva, trechos);
    }

    private static ProblemaDeEntrada problemaDaTriagem(TriagemDaEntrada triagem) {
        if (triagem.motivo() == MotivoEntradaInvalida.IDIOMAS_DIFERENTES) {
            return ProblemaDeEntrada.idiomasDiferentes(
                    DetectorDeIdioma.nome(triagem.idiomaOriginal()), DetectorDeIdioma.nome(triagem.idiomaEditado()));
        }
        return ProblemaDeEntrada.de(triagem.motivo(), triagem.campo());
    }

    // Corta texto gigante antes da normalização: as regex não podem rodar sobre megabytes.
    private String limitar(String texto, CampoEntrada campo) {
        if (texto != null && texto.length() > validador.maximoCaracteres() * 2) {
            recusar(ProblemaDeEntrada.textoLongo(campo, validador.maximoCaracteres()));
        }
        return texto;
    }

    private void recusar(ProblemaDeEntrada problema) {
        throw new EntradaInvalidaException(problema);
    }

    private String resolverTexto(EntradaTexto entradaTexto) {
        if (entradaTexto.ehArquivo()) {
            return extracaoDocumentoPort.extrairTexto(entradaTexto.arquivo(), entradaTexto.nomeArquivo());
        }
        return entradaTexto.texto();
    }
}
