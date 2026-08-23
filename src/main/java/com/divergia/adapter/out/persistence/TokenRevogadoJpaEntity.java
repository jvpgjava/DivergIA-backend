package com.divergia.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "token_revogado")
public class TokenRevogadoJpaEntity {

    @Id
    private String jti;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    protected TokenRevogadoJpaEntity() {
    }

    public TokenRevogadoJpaEntity(String jti, Instant expiraEm) {
        this.jti = jti;
        this.expiraEm = expiraEm;
    }

    public String getJti() {
        return jti;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }
}
