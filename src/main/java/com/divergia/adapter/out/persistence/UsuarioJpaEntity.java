package com.divergia.adapter.out.persistence;

import jakarta.persistence.Column;
import com.divergia.domain.model.Papel;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class UsuarioJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "foto_url")
    private String fotoUrl;

    // updatable = false: a aplicação só grava o papel ao criar a conta. Mudar de papel é feito por script
    // direto no banco, e um save() de senha/e-mail/foto nunca desfaz uma promoção feita nesse meio tempo.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private Papel papel;

    protected UsuarioJpaEntity() {
    }

    public UsuarioJpaEntity(UUID id, String nome, String email, String senhaHash, Instant criadoEm, String fotoUrl) {
        this(id, nome, email, senhaHash, criadoEm, fotoUrl, Papel.USUARIO);
    }

    public UsuarioJpaEntity(
            UUID id, String nome, String email, String senhaHash, Instant criadoEm, String fotoUrl, Papel papel) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.criadoEm = criadoEm;
        this.fotoUrl = fotoUrl;
        this.papel = papel;
    }

    public Papel getPapel() {
        return papel;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }
}
