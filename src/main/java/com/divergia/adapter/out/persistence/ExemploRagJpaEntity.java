package com.divergia.adapter.out.persistence;

import com.divergia.domain.model.OrigemExemplo;
import com.divergia.domain.model.TipoDesvio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exemplo_rag")
public class ExemploRagJpaEntity {

    @Id
    private UUID id;

    @Column(name = "texto_original", nullable = false)
    private String textoOriginal;

    @Column(name = "texto_editado", nullable = false)
    private String textoEditado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_desvio", nullable = false, length = 20)
    private TipoDesvio tipoDesvio;

    @Type(VectorUserType.class)
    @Column(name = "embedding", nullable = false, columnDefinition = "vector(768)")
    private float[] embedding;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrigemExemplo origem;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected ExemploRagJpaEntity() {
    }

    public ExemploRagJpaEntity(
            UUID id,
            String textoOriginal,
            String textoEditado,
            TipoDesvio tipoDesvio,
            float[] embedding,
            OrigemExemplo origem,
            Instant criadoEm) {
        this.id = id;
        this.textoOriginal = textoOriginal;
        this.textoEditado = textoEditado;
        this.tipoDesvio = tipoDesvio;
        this.embedding = embedding;
        this.origem = origem;
        this.criadoEm = criadoEm;
    }

    public UUID getId() {
        return id;
    }

    public String getTextoOriginal() {
        return textoOriginal;
    }

    public String getTextoEditado() {
        return textoEditado;
    }

    public TipoDesvio getTipoDesvio() {
        return tipoDesvio;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public OrigemExemplo getOrigem() {
        return origem;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
