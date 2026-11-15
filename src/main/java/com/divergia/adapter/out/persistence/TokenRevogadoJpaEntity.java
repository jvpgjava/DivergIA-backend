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

    @Column(name = "revogado_em", nullable = false)
    private Instant revogadoEm;

    protected TokenRevogadoJpaEntity() {
    }

    public TokenRevogadoJpaEntity(String jti, Instant revogadoEm) {
        this.jti = jti;
        this.revogadoEm = revogadoEm;
    }

    public String getJti() {
        return jti;
    }

    public Instant getRevogadoEm() {
        return revogadoEm;
    }
}
