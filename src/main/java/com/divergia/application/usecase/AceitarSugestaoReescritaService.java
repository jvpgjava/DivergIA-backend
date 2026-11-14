package com.divergia.application.usecase;

import com.divergia.application.port.in.AceitarSugestaoReescritaUseCase;
import com.divergia.application.port.out.AnaliseRepositoryPort;
import com.divergia.application.port.out.TrechoDerivaRepositoryPort;
import com.divergia.domain.model.Analise;
import com.divergia.domain.model.TrechoDeriva;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AceitarSugestaoReescritaService implements AceitarSugestaoReescritaUseCase {

    private final TrechoDerivaRepositoryPort trechoDerivaRepository;
    private final AnaliseRepositoryPort analiseRepository;

    public AceitarSugestaoReescritaService(
            TrechoDerivaRepositoryPort trechoDerivaRepository, AnaliseRepositoryPort analiseRepository) {
        this.trechoDerivaRepository = trechoDerivaRepository;
        this.analiseRepository = analiseRepository;
    }

    @Override
    public void aceitar(UUID usuarioId, UUID trechoDerivaId, String textoEscolhido) {
        TrechoDeriva trecho = trechoDerivaRepository.buscarPorId(trechoDerivaId)
                .orElseThrow(TrechoDerivaNaoEncontradoException::new);

        Analise analise = analiseRepository.buscarPorId(trecho.analiseId())
                .orElseThrow(TrechoDerivaNaoEncontradoException::new);

        if (!analise.pertenceA(usuarioId)) {
            throw new AcessoNaoAutorizadoException();
        }

        trechoDerivaRepository.salvar(trecho.comSugestaoAceita(textoEscolhido));
    }
}
