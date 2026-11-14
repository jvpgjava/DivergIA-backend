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
class UsuarioPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioJpaRepository repository;

    @Test
    void devePersistirEBuscarUsuarioPorId() {
        UsuarioJpaEntity usuario = new UsuarioJpaEntity(
                UUID.randomUUID(), "Ana Teste", "ana.teste+" + UUID.randomUUID() + "@example.com",
                "hash-bcrypt-fake", Instant.now(), null);

        entityManager.persistAndFlush(usuario);
        entityManager.clear();

        Optional<UsuarioJpaEntity> encontrado = repository.findById(usuario.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Ana Teste");
        assertThat(encontrado.get().getEmail()).isEqualTo(usuario.getEmail());
        assertThat(encontrado.get().getSenhaHash()).isEqualTo("hash-bcrypt-fake");
    }

    @Test
    void deveBuscarUsuarioPorEmailEIndicarExistencia() {
        String email = "busca-por-email+" + UUID.randomUUID() + "@example.com";
        UsuarioJpaEntity usuario = new UsuarioJpaEntity(
                UUID.randomUUID(), "Bia Teste", email, "hash-bcrypt-fake", Instant.now(), null);

        entityManager.persistAndFlush(usuario);
        entityManager.clear();

        assertThat(repository.existsByEmail(email)).isTrue();
        assertThat(repository.existsByEmail("nao-cadastrado@example.com")).isFalse();
        assertThat(repository.findByEmail(email)).isPresent();
        assertThat(repository.findByEmail("nao-cadastrado@example.com")).isEmpty();
    }

    @Test
    void buscaIgnoraACaixaEOBancoNaoAceitaDoisEmailsIguaisEmCaixasDiferentes() {
        String sufixo = UUID.randomUUID().toString();
        entityManager.persistAndFlush(new UsuarioJpaEntity(
                UUID.randomUUID(), "Caixa", "Ana+" + sufixo + "@Example.com", "hash", Instant.now(), null));
        entityManager.clear();

        assertThat(repository.existsByEmailIgnoreCase("ana+" + sufixo + "@example.com")).isTrue();
        assertThat(repository.findFirstByEmailIgnoreCaseOrderByCriadoEmAsc("ANA+" + sufixo + "@EXAMPLE.COM")).isPresent();

        UsuarioJpaEntity duplicado = new UsuarioJpaEntity(
                UUID.randomUUID(), "Duplicada", "ana+" + sufixo + "@example.com", "hash", Instant.now(), null);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> entityManager.persistAndFlush(duplicado))
                .isInstanceOf(Exception.class);
    }
}
