package com.divergia.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Gera tokens de uso único de alta entropia (para recuperação de senha) e
 * calcula seu hash para armazenamento — o valor bruto do token nunca é
 * persistido, só seu hash SHA-256, do mesmo jeito que uma senha nunca é
 * guardada em texto plano.
 */
public class GeradorDeTokenSeguro {

    private static final int TAMANHO_EM_BYTES = 32;
    private final SecureRandom random = new SecureRandom();

    public String gerar() {
        byte[] bytes = new byte[TAMANHO_EM_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String tokenBruto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tokenBruto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível na JVM", e);
        }
    }
}
