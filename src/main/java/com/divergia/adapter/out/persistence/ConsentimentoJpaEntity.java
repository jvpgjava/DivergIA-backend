package com.divergia.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consentimento")
public class ConsentimentoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "manter_historico", nullable = false)
    private boolean manterHistorico;

    @Column(name = "contribuir_para_rag", nullable = false)
    private boolean contribuirParaRag;

    @Column(name = "concedido_em", nullable = false)
    private Instant concedidoEm;

    protected ConsentimentoJpaEntity() {
    }

    public ConsentimentoJpaEntity(
            UUID id, UUID usuarioId, boolean manterHistorico, boolean contribuirParaRag, Instant concedidoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.manterHistorico = manterHistorico;
        this.contribuirParaRag = contribuirParaRag;
        this.concedidoEm = concedidoEm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public boolean isManterHistorico() {
        return manterHistorico;
    }

    public boolean isContribuirParaRag() {
        return contribuirParaRag;
    }

    public Instant getConcedidoEm() {
        return concedidoEm;
    }
}
