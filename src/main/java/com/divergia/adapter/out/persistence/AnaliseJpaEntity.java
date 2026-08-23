package com.divergia.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analise")
public class AnaliseJpaEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "texto_original")
    private String textoOriginal;

    @Column(name = "texto_editado")
    private String textoEditado;

    @Column(name = "manter_historico", nullable = false)
    private boolean manterHistorico;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected AnaliseJpaEntity() {
    }

    public AnaliseJpaEntity(
            UUID id,
            UUID usuarioId,
            String textoOriginal,
            String textoEditado,
            boolean manterHistorico,
            Instant criadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.textoOriginal = textoOriginal;
        this.textoEditado = textoEditado;
        this.manterHistorico = manterHistorico;
        this.criadoEm = criadoEm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getTextoOriginal() {
        return textoOriginal;
    }

    public String getTextoEditado() {
        return textoEditado;
    }

    public boolean isManterHistorico() {
        return manterHistorico;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
