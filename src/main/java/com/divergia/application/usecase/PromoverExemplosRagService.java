package com.divergia.application.usecase;

import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.ConsentimentoRepositoryPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.Consentimento;
import com.divergia.domain.model.OrigemExemplo;
import com.divergia.domain.model.TrechoDeriva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Job assíncrono que promove trechos de deriva ainda não promovidos para a
 * base de exemplos compartilhada (RAG) — só quando o usuário dono da análise
 * consentiu explicitamente em contribuir com isso ({@code contribuirParaRag}),
 * um consentimento distinto de só manter o próprio histórico.
 */
@Service
public class PromoverExemplosRagService {

    private static final Logger log = LoggerFactory.getLogger(PromoverExemplosRagService.class);

    private final TrechoDerivaRepositoryPort trechoDerivaRepository;
    private final AnaliseRepositoryPort analiseRepository;
    private final ConsentimentoRepositoryPort consentimentoRepository;
    private final VectorStorePort vectorStorePort;

    public PromoverExemplosRagService(
            TrechoDerivaRepositoryPort trechoDerivaRepository,
            AnaliseRepositoryPort analiseRepository,
            ConsentimentoRepositoryPort consentimentoRepository,
            VectorStorePort vectorStorePort) {
        this.trechoDerivaRepository = trechoDerivaRepository;
        this.analiseRepository = analiseRepository;
        this.consentimentoRepository = consentimentoRepository;
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
        return analiseRepository.buscarPorId(trecho.analiseId())
                .filter(analise -> consentiuContribuirParaRag(analise.usuarioId()))
                .map(analise -> {
                    vectorStorePort.salvar(
                            trecho.trechoOriginal(), trecho.trechoEditado(), trecho.tipoDesvio(),
                            OrigemExemplo.PROMOVIDO_DE_ANALISE);
                    trechoDerivaRepository.marcarComoPromovidoParaRag(trecho.id());
                    return true;
                })
                .orElseGet(() -> {
                    // usuário não consentiu (ou análise não existe mais) — marca como
                    // "resolvido" mesmo assim, para não reprocessar este trecho todo dia.
                    trechoDerivaRepository.marcarComoPromovidoParaRag(trecho.id());
                    return false;
                });
    }

    private boolean consentiuContribuirParaRag(UUID usuarioId) {
        return consentimentoRepository.buscarMaisRecentePorUsuarioId(usuarioId)
                .map(Consentimento::contribuirParaRag)
                .orElse(false);
    }
}
