package com.divergia.domain.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Exemplo de par (texto original, texto editado) usado como referência pela
 * busca vetorial (RAG) ao avaliar uma nova análise.
 */
public record ExemploRag(
        UUID id,
        String textoOriginal,
        String textoEditado,
        TipoDesvio tipoDesvio,
        float[] embedding,
        OrigemExemplo origem,
        Instant criadoEm) {

    public ExemploRag {
        Objects.requireNonNull(id, "id não pode ser nulo");
        if (textoOriginal == null || textoOriginal.isBlank()) {
            throw new IllegalArgumentException("textoOriginal não pode ser vazio");
        }
        if (textoEditado == null || textoEditado.isBlank()) {
            throw new IllegalArgumentException("textoEditado não pode ser vazio");
        }
        Objects.requireNonNull(tipoDesvio, "tipoDesvio não pode ser nulo");
        Objects.requireNonNull(embedding, "embedding não pode ser nulo");
        Objects.requireNonNull(origem, "origem não pode ser nula");
        Objects.requireNonNull(criadoEm, "criadoEm não pode ser nulo");
    }

    // equals/hashCode/toString sobrescritos manualmente: o equals gerado pelo
    // record compara o array de embedding por referência, não por conteúdo.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExemploRag other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && Objects.equals(textoOriginal, other.textoOriginal)
                && Objects.equals(textoEditado, other.textoEditado)
                && tipoDesvio == other.tipoDesvio
                && Arrays.equals(embedding, other.embedding)
                && origem == other.origem
                && Objects.equals(criadoEm, other.criadoEm);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, textoOriginal, textoEditado, tipoDesvio, origem, criadoEm);
        return 31 * result + Arrays.hashCode(embedding);
    }

    @Override
    public String toString() {
        return "ExemploRag[id=%s, tipoDesvio=%s, origem=%s, embedding=float[%d], criadoEm=%s]"
                .formatted(id, tipoDesvio, origem, embedding.length, criadoEm);
    }
}
