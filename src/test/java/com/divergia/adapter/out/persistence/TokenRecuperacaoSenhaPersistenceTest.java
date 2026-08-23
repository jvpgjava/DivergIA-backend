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
class TokenRecuperacaoSenhaPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TokenRecuperacaoSenhaJpaRepository repository;

    @Test
    void devePersistirEBuscarTokenPorHash() {
        UsuarioJpaEntity usuario = entityManager.persistAndFlush(new UsuarioJpaEntity(
                UUID.randomUUID(), "Usuário Teste", "token-recuperacao+" + UUID.randomUUID() + "@example.com",
                "hash-fake", Instant.now()));

        Instant agora = Instant.now();
        TokenRecuperacaoSenhaJpaEntity token = new TokenRecuperacaoSenhaJpaEntity(
                UUID.randomUUID(), usuario.getId(), "hash-do-token-abc123", agora, agora.plusSeconds(1800), null);

        entityManager.persistAndFlush(token);
        entityManager.clear();

        Optional<TokenRecuperacaoSenhaJpaEntity> encontrado = repository.findByTokenHash("hash-do-token-abc123");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getUsuarioId()).isEqualTo(usuario.getId());
        assertThat(encontrado.get().getUsadoEm()).isNull();
    }
}
