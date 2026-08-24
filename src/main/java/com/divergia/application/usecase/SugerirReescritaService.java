package com.divergia.application.usecase;

import com.divergia.application.port.in.SugerirReescritaUseCase;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.LlmPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.application.port.out.VectorStorePort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.TrechoDeriva;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SugerirReescritaService implements SugerirReescritaUseCase {

    private final TrechoDerivaRepositoryPort trechoDerivaRepository;
    private final AnaliseRepositoryPort analiseRepository;
    private final VectorStorePort vectorStorePort;
    private final LlmPort llmPort;
    private final int quantidadeExemplosRag;

    public SugerirReescritaService(
            TrechoDerivaRepositoryPort trechoDerivaRepository,
            AnaliseRepositoryPort analiseRepository,
            VectorStorePort vectorStorePort,
            LlmPort llmPort,
            @Value("${divergia.analise.quantidade-exemplos-rag:5}") int quantidadeExemplosRag) {
        this.trechoDerivaRepository = trechoDerivaRepository;
        this.analiseRepository = analiseRepository;
        this.vectorStorePort = vectorStorePort;
        this.llmPort = llmPort;
        this.quantidadeExemplosRag = quantidadeExemplosRag;
    }

    @Override
    public String sugerir(UUID usuarioId, UUID trechoDerivaId) {
        TrechoDeriva trecho = trechoDerivaRepository.buscarPorId(trechoDerivaId)
                .orElseThrow(TrechoDerivaNaoEncontradoException::new);

        Analise analise = analiseRepository.buscarPorId(trecho.analiseId())
                .orElseThrow(TrechoDerivaNaoEncontradoException::new);

        if (!analise.usuarioId().equals(usuarioId)) {
            throw new AcessoNaoAutorizadoException();
        }

        List<ExemploRag> exemplos = vectorStorePort.buscarSimilares(
                trecho.trechoOriginal() + "\n" + trecho.trechoEditado(), quantidadeExemplosRag);

        return llmPort.sugerirReescrita(
                trecho.trechoOriginal(), trecho.trechoEditado(), trecho.tipoDesvio(), trecho.explicacao(), exemplos);
    }
}
