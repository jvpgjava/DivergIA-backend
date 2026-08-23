package com.divergia.adapter.out.persistence;

import com.divergia.domain.model.TipoDesvio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "trecho_deriva")
public class TrechoDerivaJpaEntity {

    @Id
    private UUID id;

    @Column(name = "analise_id", nullable = false)
    private UUID analiseId;

    @Column(name = "trecho_original", nullable = false)
    private String trechoOriginal;

    @Column(name = "trecho_editado", nullable = false)
    private String trechoEditado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_desvio", nullable = false, length = 20)
    private TipoDesvio tipoDesvio;

    @Column(nullable = false)
    private String explicacao;

    @Column(nullable = false)
    private double intensidade;

    protected TrechoDerivaJpaEntity() {
    }

    public TrechoDerivaJpaEntity(
            UUID id,
            UUID analiseId,
            String trechoOriginal,
            String trechoEditado,
            TipoDesvio tipoDesvio,
            String explicacao,
            double intensidade) {
        this.id = id;
        this.analiseId = analiseId;
        this.trechoOriginal = trechoOriginal;
        this.trechoEditado = trechoEditado;
        this.tipoDesvio = tipoDesvio;
        this.explicacao = explicacao;
        this.intensidade = intensidade;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAnaliseId() {
        return analiseId;
    }

    public String getTrechoOriginal() {
        return trechoOriginal;
    }

    public String getTrechoEditado() {
        return trechoEditado;
    }

    public TipoDesvio getTipoDesvio() {
        return tipoDesvio;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public double getIntensidade() {
        return intensidade;
    }
}
