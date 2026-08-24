package com.divergia.application.usecase;

import com.divergia.application.port.out.ConsentimentoRepositoryPort;
import com.divergia.domain.model.Consentimento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ObterConsentimentoServiceTest {

    @Mock
    private ConsentimentoRepositoryPort consentimentoRepository;

    @Test
    void deveDevolverOConsentimentoMaisRecenteQuandoExiste() {
        UUID usuarioId = UUID.randomUUID();
        Consentimento existente = new Consentimento(UUID.randomUUID(), usuarioId, true, true, Instant.now());
        given(consentimentoRepository.buscarMaisRecentePorUsuarioId(usuarioId)).willReturn(Optional.of(existente));

        ObterConsentimentoService service = new ObterConsentimentoService(consentimentoRepository);
        Consentimento resultado = service.obter(usuarioId);

        assertThat(resultado).isEqualTo(existente);
    }

    @Test
    void deveDevolverPadraoPrivacyByDefaultQuandoUsuarioNuncaDefiniuConsentimento() {
        UUID usuarioId = UUID.randomUUID();
        given(consentimentoRepository.buscarMaisRecentePorUsuarioId(usuarioId)).willReturn(Optional.empty());

        ObterConsentimentoService service = new ObterConsentimentoService(consentimentoRepository);
        Consentimento resultado = service.obter(usuarioId);

        assertThat(resultado.manterHistorico()).isFalse();
        assertThat(resultado.contribuirParaRag()).isFalse();
        assertThat(resultado.usuarioId()).isEqualTo(usuarioId);
    }
}
