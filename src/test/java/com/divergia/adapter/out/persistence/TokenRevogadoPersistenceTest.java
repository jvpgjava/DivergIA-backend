package com.divergia.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TokenRevogadoPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TokenRevogadoJpaRepository repository;

    @Test
    void devePersistirEVerificarExistenciaDeTokenRevogado() {
        String jti = UUID.randomUUID().toString();
        entityManager.persistAndFlush(new TokenRevogadoJpaEntity(jti, Instant.now().plusSeconds(900)));
        entityManager.clear();

        assertThat(repository.existsById(jti)).isTrue();
        assertThat(repository.existsById(UUID.randomUUID().toString())).isFalse();
    }
}
