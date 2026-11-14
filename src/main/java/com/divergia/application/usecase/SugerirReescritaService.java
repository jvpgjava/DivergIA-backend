package com.divergia.application.usecase;

import com.divergia.application.port.in.SugerirReescritaUseCase;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.LlmException;
import com.divergia.application.port.out.LlmPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.ExemploRag;
import com.divergia.domain.model.TrechoDeriva;
import com.divergia.domain.service.GuardaDeSugestoes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SugerirReescritaService implements SugerirReescritaUseCase {

    private static final Logger log = LoggerFactory.getLogger(SugerirReescritaService.class);
    static final int TENTATIVAS = 2;

    private final TrechoDerivaRepositoryPort trechoDerivaRepository;
    private final AnaliseRepositoryPort analiseRepository;
    private final BuscadorDeExemplosRag buscadorDeExemplos;
    private final LlmPort llmPort;
    private final GuardaDeSugestoes guarda = new GuardaDeSugestoes();
    private final boolean juizHabilitado;

    public SugerirReescritaService(
            TrechoDerivaRepositoryPort trechoDerivaRepository,
            AnaliseRepositoryPort analiseRepository,
            BuscadorDeExemplosRag buscadorDeExemplos,
            LlmPort llmPort,
            @Value("${divergia.ia.juiz-fidelidade.habilitado:true}") boolean juizHabilitado) {
        this.trechoDerivaRepository = trechoDerivaRepository;
        this.analiseRepository = analiseRepository;
        this.buscadorDeExemplos = buscadorDeExemplos;
        this.llmPort = llmPort;
        this.juizHabilitado = juizHabilitado;
    }

    @Override
    public List<String> sugerir(UUID usuarioId, UUID trechoDerivaId) {
        TrechoDeriva trecho = trechoDerivaRepository.buscarPorId(trechoDerivaId)
                .orElseThrow(TrechoDerivaNaoEncontradoException::new);

        Analise analise = analiseRepository.buscarPorId(trecho.analiseId())
                .orElseThrow(TrechoDerivaNaoEncontradoException::new);

        if (!analise.pertenceA(usuarioId)) {
            throw new AcessoNaoAutorizadoException();
        }

        List<ExemploRag> exemplos = buscadorDeExemplos.buscar(trecho.trechoOriginal(), trecho.trechoEditado());
        LlmException ultimaFalha = null;
        for (int tentativa = 1; tentativa <= TENTATIVAS; tentativa++) {
            try {
                List<String> aprovadas = gerarEFiltrar(trecho, exemplos);
                if (!aprovadas.isEmpty()) {
                    return aprovadas;
                }
                log.info("Reescrita: nenhuma alternativa passou pelas guardas (tentativa {}/{})", tentativa, TENTATIVAS);
            } catch (LlmException e) {
                ultimaFalha = e;
                log.warn("Reescrita: falha do modelo (tentativa {}/{}): {}", tentativa, TENTATIVAS, e.getMessage());
            }
        }
        if (ultimaFalha != null) {
            throw ultimaFalha;
        }
        // Melhor nenhuma sugestão do que uma que muda o sentido de novo.
        throw new SugestaoIndisponivelException();
    }

    private List<String> gerarEFiltrar(TrechoDeriva trecho, List<ExemploRag> exemplos) {
        List<String> candidatas = llmPort.sugerirReescrita(
                trecho.trechoOriginal(), trecho.trechoEditado(), trecho.tipoDesvio(), trecho.explicacao(), exemplos);

        GuardaDeSugestoes.Resultado filtradas = guarda.filtrar(trecho.trechoOriginal(), trecho.trechoEditado(), candidatas);
        if (!filtradas.descartes().isEmpty()) {
            log.info("Reescrita: guarda descartou {}", filtradas.descartes());
        }
        if (filtradas.aprovadas().isEmpty() || !juizHabilitado) {
            return filtradas.aprovadas();
        }

        List<Boolean> vereditos = llmPort.verificarFidelidade(trecho.trechoOriginal(), filtradas.aprovadas());
        // Juiz indisponível nunca derruba a reescrita: vale só a guarda determinística.
        if (vereditos.size() != filtradas.aprovadas().size()) {
            log.info("Reescrita: juiz de fidelidade indisponível; seguindo só com a guarda determinística");
            return filtradas.aprovadas();
        }
        List<String> fieis = new ArrayList<>();
        for (int i = 0; i < vereditos.size(); i++) {
            if (Boolean.TRUE.equals(vereditos.get(i))) {
                fieis.add(filtradas.aprovadas().get(i));
            }
        }
        if (fieis.size() < filtradas.aprovadas().size()) {
            log.info("Reescrita: juiz reprovou {} de {} alternativas",
                    filtradas.aprovadas().size() - fieis.size(), filtradas.aprovadas().size());
        }
        return fieis;
    }
}
