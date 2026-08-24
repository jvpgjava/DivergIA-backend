package com.divergia.adapter.out.persistence;

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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrechoDerivaPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TrechoDerivaJpaRepository repository;

    @Test
    void devePersistirEBuscarTrechoDerivaDeUmaAnalise() {
        UsuarioJpaEntity usuario = entityManager.persistAndFlush(new UsuarioJpaEntity(
                UUID.randomUUID(), "Usuário Teste", "trecho+" + UUID.randomUUID() + "@example.com",
                "hash-fake", Instant.now()));
        AnaliseJpaEntity analise = entityManager.persistAndFlush(new AnaliseJpaEntity(
                UUID.randomUUID(), usuario.getId(), "original", "editado", true, Instant.now()));

        TrechoDerivaJpaEntity trecho = new TrechoDerivaJpaEntity(
                UUID.randomUUID(), analise.getId(), "trecho original", "trecho editado",
                TipoDesvio.INTENSIDADE, "a intensidade da afirmação foi ampliada", 0.75, false);

        entityManager.persistAndFlush(trecho);
        entityManager.clear();

        Optional<TrechoDerivaJpaEntity> encontrado = repository.findById(trecho.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getAnaliseId()).isEqualTo(analise.getId());
        assertThat(encontrado.get().getTipoDesvio()).isEqualTo(TipoDesvio.INTENSIDADE);
        assertThat(encontrado.get().getIntensidade()).isEqualTo(0.75);
    }

    @Test
    void deveBuscarTrechosPorUsuarioIdEFiltrarNaoPromovidos() {
        UsuarioJpaEntity usuario = entityManager.persistAndFlush(new UsuarioJpaEntity(
                UUID.randomUUID(), "Usuário Teste", "trecho-usuario+" + UUID.randomUUID() + "@example.com",
                "hash-fake", Instant.now()));
        AnaliseJpaEntity analise = entityManager.persistAndFlush(new AnaliseJpaEntity(
                UUID.randomUUID(), usuario.getId(), "original", "editado", true, Instant.now()));

        TrechoDerivaJpaEntity naoPromovido = entityManager.persistAndFlush(new TrechoDerivaJpaEntity(
                UUID.randomUUID(), analise.getId(), "trecho A", "trecho A editado",
                TipoDesvio.SENTIDO, "explicacao", 0.5, false));
        entityManager.persistAndFlush(new TrechoDerivaJpaEntity(
                UUID.randomUUID(), analise.getId(), "trecho B", "trecho B editado",
                TipoDesvio.POSICAO, "explicacao", 0.3, true));
        entityManager.clear();

        assertThat(repository.findByUsuarioId(usuario.getId())).hasSize(2);

        var naoPromovidos = repository.findByPromovidoParaRagFalse();
        assertThat(naoPromovidos).extracting(TrechoDerivaJpaEntity::getId).contains(naoPromovido.getId());
        assertThat(naoPromovidos).allMatch(t -> !t.isPromovidoParaRag());
    }
}
