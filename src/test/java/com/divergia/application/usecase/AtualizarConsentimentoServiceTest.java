package com.divergia.application.usecase;

import com.divergia.application.port.out.ConsentimentoRepositoryPort;
import com.divergia.domain.model.Consentimento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AtualizarConsentimentoServiceTest {

    @Mock
    private ConsentimentoRepositoryPort consentimentoRepository;

    @Test
    void deveCriarNovoRegistroDeConsentimentoComOsValoresInformados() {
        UUID usuarioId = UUID.randomUUID();
        given(consentimentoRepository.salvar(any(Consentimento.class))).willAnswer(inv -> inv.getArgument(0));

        AtualizarConsentimentoService service = new AtualizarConsentimentoService(consentimentoRepository);
        Consentimento resultado = service.atualizar(usuarioId, true, true);

        assertThat(resultado.usuarioId()).isEqualTo(usuarioId);
        assertThat(resultado.manterHistorico()).isTrue();
        assertThat(resultado.contribuirParaRag()).isTrue();

        ArgumentCaptor<Consentimento> captor = ArgumentCaptor.forClass(Consentimento.class);
        verify(consentimentoRepository).salvar(captor.capture());
        assertThat(captor.getValue().id()).isNotNull();
    }
}
