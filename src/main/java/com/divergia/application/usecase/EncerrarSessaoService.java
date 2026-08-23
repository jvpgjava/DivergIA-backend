package com.divergia.application.usecase;

import com.divergia.application.port.in.EncerrarSessaoUseCase;
import com.divergia.application.port.out.TokenPort;
import com.divergia.application.port.out.TokenRevogadoPort;
import com.divergia.domain.model.TokenAcesso;
import org.springframework.stereotype.Service;

@Service
public class EncerrarSessaoService implements EncerrarSessaoUseCase {

    private final TokenPort tokenPort;
    private final TokenRevogadoPort tokenRevogadoPort;

    public EncerrarSessaoService(TokenPort tokenPort, TokenRevogadoPort tokenRevogadoPort) {
        this.tokenPort = tokenPort;
        this.tokenRevogadoPort = tokenRevogadoPort;
    }

    @Override
    public void encerrar(String tokenBruto) {
        TokenAcesso token = tokenPort.validar(tokenBruto);
        tokenRevogadoPort.revogar(token.jti(), token.expiraEm());
    }
}
