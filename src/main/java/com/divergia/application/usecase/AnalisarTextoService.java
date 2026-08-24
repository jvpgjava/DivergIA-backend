package com.divergia.application.usecase;

import com.divergia.application.port.in.AnalisarTextoUseCase;
import com.divergia.application.port.in.EntradaAnalise;
import com.divergia.application.port.in.EntradaTexto;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.ExtracaoDocumentoPort;
import com.divergia.application.port.out.LlmPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.ResultadoAnalise;
import com.divergia.domain.model.TrechoDeriva;
import com.divergia.domain.service.PoliticaRetencaoDeTexto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnalisarTextoService implements AnalisarTextoUseCase {

    private final ExtracaoDocumentoPort extracaoDocumentoPort;
    private final VectorStorePort vectorStorePort;
    private final LlmPort llmPort;
    private final AnaliseRepositoryPort analiseRepository;
    private final TrechoDerivaRepositoryPort trechoDerivaRepository;
    private final PoliticaRetencaoDeTexto politicaRetencao = new PoliticaRetencaoDeTexto();
    private final int quantidadeExemplosRag;

    public AnalisarTextoService(
            ExtracaoDocumentoPort extracaoDocumentoPort,
            VectorStorePort vectorStorePort,
            LlmPort llmPort,
            AnaliseRepositoryPort analiseRepository,
            TrechoDerivaRepositoryPort trechoDerivaRepository,
            @Value("${divergia.analise.quantidade-exemplos-rag:5}") int quantidadeExemplosRag) {
        this.extracaoDocumentoPort = extracaoDocumentoPort;
        this.vectorStorePort = vectorStorePort;
        this.llmPort = llmPort;
        this.analiseRepository = analiseRepository;
        this.trechoDerivaRepository = trechoDerivaRepository;
        this.quantidadeExemplosRag = quantidadeExemplosRag;
    }

    @Override
    @Transactional
    public ResultadoAnalise analisar(EntradaAnalise entrada) {
        String textoOriginal = resolverTexto(entrada.original());
        String textoEditado = resolverTexto(entrada.editado());

        List<ExemploRag> exemplos = vectorStorePort.buscarSimilares(
                textoOriginal + "\n" + textoEditado, quantidadeExemplosRag);
        List<AvaliacaoDeDeriva> avaliacoes = llmPort.avaliarDerivas(textoOriginal, textoEditado, exemplos);

        Analise analiseBruta = new Analise(
                UUID.randomUUID(), entrada.usuarioId(), textoOriginal, textoEditado,
                entrada.manterHistorico(), Instant.now());
        Analise analiseParaPersistir = politicaRetencao.aplicar(analiseBruta);
        Analise analiseSalva = analiseRepository.salvar(analiseParaPersistir);

        List<TrechoDeriva> trechos = new ArrayList<>();
        for (AvaliacaoDeDeriva avaliacao : avaliacoes) {
            TrechoDeriva trecho = new TrechoDeriva(
                    UUID.randomUUID(),
                    analiseSalva.id(),
                    avaliacao.trechoOriginal(),
                    avaliacao.trechoEditado(),
                    avaliacao.tipoDesvio(),
                    avaliacao.explicacao(),
                    avaliacao.intensidade());
            // Os trechos citam texto bruto (original/editado) — mesma regra de
            // retenção da Analise: só persiste se houve consentimento. Ainda
            // assim aparecem no resultado devolvido nesta chamada.
            trechos.add(entrada.manterHistorico() ? trechoDerivaRepository.salvar(trecho) : trecho);
        }

        return new ResultadoAnalise(analiseSalva, trechos);
    }

    private String resolverTexto(EntradaTexto entradaTexto) {
        if (entradaTexto.ehArquivo()) {
            return extracaoDocumentoPort.extrairTexto(entradaTexto.arquivo(), entradaTexto.nomeArquivo());
        }
        return entradaTexto.texto();
    }
}
