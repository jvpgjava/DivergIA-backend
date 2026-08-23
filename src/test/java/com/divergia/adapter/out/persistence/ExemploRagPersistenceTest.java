package com.divergia.adapter.out.persistence;

import com.divergia.domain.model.OrigemExemplo;
import com.divergia.domain.model.TipoDesvio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExemploRagPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExemploRagJpaRepository repository;

    @Test
    void devePersistirEBuscarExemploRagComEmbeddingVetorial() {
        float[] embedding = new float[768];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = (float) Math.sin(i) * 0.01f;
        }

        ExemploRagJpaEntity exemplo = new ExemploRagJpaEntity(
                UUID.randomUUID(),
                "texto original do estudo",
                "texto editado do estudo",
                TipoDesvio.SENTIDO,
                embedding,
                OrigemExemplo.ESTUDO_OXFORD_POTSDAM,
                Instant.now());

        entityManager.persistAndFlush(exemplo);
        entityManager.clear();

        Optional<ExemploRagJpaEntity> encontrado = repository.findById(exemplo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTipoDesvio()).isEqualTo(TipoDesvio.SENTIDO);
        assertThat(encontrado.get().getOrigem()).isEqualTo(OrigemExemplo.ESTUDO_OXFORD_POTSDAM);
        float[] embeddingLido = encontrado.get().getEmbedding();
        assertThat(embeddingLido).hasSize(768);
        for (int i = 0; i < embedding.length; i++) {
            assertThat(embeddingLido[i]).isCloseTo(embedding[i], within(1e-4f));
        }
    }
}
