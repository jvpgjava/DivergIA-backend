package com.divergia.adapter.out.persistence;

import com.divergia.application.port.out.TokenRevogadoPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TokenRevogadoRepositoryAdapter implements TokenRevogadoPort {

    private final TokenRevogadoJpaRepository repository;

    public TokenRevogadoRepositoryAdapter(TokenRevogadoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void revogar(String jti, Instant expiraEm) {
        repository.save(new TokenRevogadoJpaEntity(jti, expiraEm));
    }

    @Override
    public boolean estaRevogado(String jti) {
        return repository.existsById(jti);
    }
}
