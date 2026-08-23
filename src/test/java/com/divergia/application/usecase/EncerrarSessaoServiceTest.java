package com.divergia.application.usecase;

import com.divergia.application.port.out.TokenPort;
import com.divergia.application.port.out.TokenRevogadoPort;
import com.divergia.domain.model.TokenAcesso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EncerrarSessaoServiceTest {

    @Mock
    private TokenPort tokenPort;

    @Mock
    private TokenRevogadoPort tokenRevogadoPort;

    @Test
    void deveRevogarOJtiDoTokenValidado() {
        EncerrarSessaoService service = new EncerrarSessaoService(tokenPort, tokenRevogadoPort);
        Instant expiraEm = Instant.now().plusSeconds(900);
        TokenAcesso token = new TokenAcesso("jwt-bruto", "jti-123", UUID.randomUUID(), expiraEm);
        given(tokenPort.validar("jwt-bruto")).willReturn(token);

        service.encerrar("jwt-bruto");

        verify(tokenRevogadoPort).revogar("jti-123", expiraEm);
    }
}
