package com.divergia.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Gera códigos de uso único (para recuperação de senha) e calcula seu hash
 * para armazenamento — o valor bruto do código nunca é persistido, só seu
 * hash SHA-256, do mesmo jeito que uma senha nunca é guardada em texto
 * plano.
 *
 * <p>6 caracteres (letras maiúsculas + números, sem 0/O nem 1/I pra não
 * confundir na hora de digitar) em vez de um token longo — pensado pra ser
 * digitado à mão a partir do e-mail, não só colado. A entropia menor
 * (~30 bits) só é segura porque o código expira rápido, é de uso único, e
 * o endpoint de redefinição está sujeito ao mesmo rate limit de
 * login/cadastro (ver {@code RateLimitingFilter}).
 */
public class GeradorDeTokenSeguro {

    private static final int TAMANHO_DO_CODIGO = 6;
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    public String gerar() {
        StringBuilder codigo = new StringBuilder(TAMANHO_DO_CODIGO);
        for (int i = 0; i < TAMANHO_DO_CODIGO; i++) {
            codigo.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return codigo.toString();
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
