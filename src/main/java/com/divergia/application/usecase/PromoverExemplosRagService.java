package com.divergia.application.usecase;

import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.OrigemExemplo;
import com.divergia.domain.model.TrechoDeriva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromoverExemplosRagService {

    private static final Logger log = LoggerFactory.getLogger(PromoverExemplosRagService.class);

    private final TrechoDerivaRepositoryPort trechoDerivaRepository;
    private final AnaliseRepositoryPort analiseRepository;
    private final VectorStorePort vectorStorePort;

    public PromoverExemplosRagService(
            TrechoDerivaRepositoryPort trechoDerivaRepository,
            AnaliseRepositoryPort analiseRepository,
            VectorStorePort vectorStorePort) {
        this.trechoDerivaRepository = trechoDerivaRepository;
        this.analiseRepository = analiseRepository;
        this.vectorStorePort = vectorStorePort;
    }

    @Scheduled(cron = "${divergia.promocao-rag.cron:0 0 3 * * *}")
    public void executar() {
        List<TrechoDeriva> candidatos = trechoDerivaRepository.buscarNaoPromovidosParaRag();
        int promovidos = 0;
        for (TrechoDeriva trecho : candidatos) {
            if (promover(trecho)) {
                promovidos++;
            }
        }
        log.info("Promoção de exemplos ao RAG: {} candidatos, {} promovidos", candidatos.size(), promovidos);
    }

    private boolean promover(TrechoDeriva trecho) {
        boolean analiseExiste = analiseRepository.buscarPorId(trecho.analiseId()).isPresent();
        if (analiseExiste) {
            vectorStorePort.salvar(
                    trecho.trechoOriginal(), trecho.trechoEditado(), trecho.tipoDesvio(),
                    OrigemExemplo.PROMOVIDO_DE_ANALISE);
        }
        // Marca mesmo sem a análise (já excluída) para não reprocessar o trecho todo dia.
        trechoDerivaRepository.marcarComoPromovidoParaRag(trecho.id());
        return analiseExiste;
    }
}
