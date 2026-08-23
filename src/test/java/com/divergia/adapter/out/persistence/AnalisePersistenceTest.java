package com.divergia.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AnalisePersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AnaliseJpaRepository repository;

    @Test
    void devePersistirEBuscarAnaliseComTextoBruto() {
        UsuarioJpaEntity usuario = entityManager.persistAndFlush(new UsuarioJpaEntity(
                UUID.randomUUID(), "Usuário Teste", "analise+" + UUID.randomUUID() + "@example.com",
                "hash-fake", Instant.now()));

        AnaliseJpaEntity analise = new AnaliseJpaEntity(
                UUID.randomUUID(), usuario.getId(), "texto original", "texto editado", true, Instant.now());

        entityManager.persistAndFlush(analise);
        entityManager.clear();

        Optional<AnaliseJpaEntity> encontrada = repository.findById(analise.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getUsuarioId()).isEqualTo(usuario.getId());
        assertThat(encontrada.get().getTextoOriginal()).isEqualTo("texto original");
        assertThat(encontrada.get().getTextoEditado()).isEqualTo("texto editado");
        assertThat(encontrada.get().isManterHistorico()).isTrue();
    }

    @Test
    void devePersistirAnaliseSemTextoBrutoQuandoNaoHaConsentimento() {
        UsuarioJpaEntity usuario = entityManager.persistAndFlush(new UsuarioJpaEntity(
                UUID.randomUUID(), "Usuário Teste", "analise-sem-texto+" + UUID.randomUUID() + "@example.com",
                "hash-fake", Instant.now()));

        AnaliseJpaEntity analise = new AnaliseJpaEntity(
                UUID.randomUUID(), usuario.getId(), null, null, false, Instant.now());

        entityManager.persistAndFlush(analise);
        entityManager.clear();

        Optional<AnaliseJpaEntity> encontrada = repository.findById(analise.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getTextoOriginal()).isNull();
        assertThat(encontrada.get().getTextoEditado()).isNull();
        assertThat(encontrada.get().isManterHistorico()).isFalse();
    }
}
