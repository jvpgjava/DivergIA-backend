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
class ConsentimentoPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ConsentimentoJpaRepository repository;

    @Test
    void devePersistirEBuscarConsentimentoDeUsuario() {
        UsuarioJpaEntity usuario = entityManager.persistAndFlush(new UsuarioJpaEntity(
                UUID.randomUUID(), "Usuário Teste", "consentimento+" + UUID.randomUUID() + "@example.com",
                "hash-fake", Instant.now()));

        ConsentimentoJpaEntity consentimento = new ConsentimentoJpaEntity(
                UUID.randomUUID(), usuario.getId(), true, false, Instant.now());

        entityManager.persistAndFlush(consentimento);
        entityManager.clear();

        Optional<ConsentimentoJpaEntity> encontrado = repository.findById(consentimento.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getUsuarioId()).isEqualTo(usuario.getId());
        assertThat(encontrado.get().isManterHistorico()).isTrue();
        assertThat(encontrado.get().isContribuirParaRag()).isFalse();
    }

    @Test
    void deveBuscarOConsentimentoMaisRecentePorUsuarioId() {
        UsuarioJpaEntity usuario = entityManager.persistAndFlush(new UsuarioJpaEntity(
                UUID.randomUUID(), "Usuário Teste", "consentimento-recente+" + UUID.randomUUID() + "@example.com",
                "hash-fake", Instant.now()));

        Instant primeiro = Instant.now();
        entityManager.persistAndFlush(
                new ConsentimentoJpaEntity(UUID.randomUUID(), usuario.getId(), false, false, primeiro));

        Instant segundo = primeiro.plusSeconds(60);
        ConsentimentoJpaEntity maisRecente = entityManager.persistAndFlush(
                new ConsentimentoJpaEntity(UUID.randomUUID(), usuario.getId(), true, true, segundo));

        entityManager.clear();

        Optional<ConsentimentoJpaEntity> encontrado =
                repository.findFirstByUsuarioIdOrderByConcedidoEmDesc(usuario.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isEqualTo(maisRecente.getId());
        assertThat(encontrado.get().isManterHistorico()).isTrue();
        assertThat(encontrado.get().isContribuirParaRag()).isTrue();
    }
}
