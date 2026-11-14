package com.divergia.domain.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class LexicoOfensivo {

    private static final Set<String> PALAVRAS = Set.of(
            "porra", "porras", "caralho", "caralhos", "caraio", "krl", "crl", "pqp", "vsf", "vtnc", "tnc",
            "fdp", "merda", "merdas", "bosta", "bostas", "cu", "cuzao", "cuzona", "foda", "fodase",
            "foder", "fodido", "fodida", "fudido", "fudida", "fuder", "puta", "putas", "puto", "putaria",
            "arrombado", "arrombada", "viado", "viadinho", "buceta", "boceta", "piroca",
            "punheta", "punheteiro", "babaca", "otario", "otaria", "imbecil", "idiota", "retardado",
            "retardada", "desgracado", "desgracada", "vagabundo", "vagabunda", "corno", "corna",
            "escroto", "escrota", "cacete",
            "fuck", "fucking", "fucked", "fucker", "motherfucker", "shit", "shitty", "bullshit", "bitch",
            "bitches", "asshole", "bastard", "dickhead", "cunt", "whore", "slut", "wtf", "stfu",
            "moron", "retard");

    private static final List<String> PREFIXOS = List.of(
            "caralh", "merd", "fodid", "fudid", "arromb", "punhet", "fuck", "motherf", "bullsh", "asshol");

    private LexicoOfensivo() {
    }

    public static Set<String> encontrar(String texto) {
        Set<String> achados = new LinkedHashSet<>();
        for (String bruta : NormalizadorDeTexto.palavrasComLeet(texto)) {
            String palavra = colapsarRepeticoes(desfazerLeet(bruta));
            if (ehOfensiva(palavra)) {
                achados.add(palavra);
            }
        }
        return achados;
    }

    // Conservador: suavizar um palavrão real é justamente deriva de intensidade, então só recusa o que é quase só ofensa.
    public static boolean soOfensa(String texto) {
        List<String> palavras = NormalizadorDeTexto.palavrasComLeet(texto);
        int ofensivas = 0;
        int conteudo = 0;
        for (String bruta : palavras) {
            String palavra = colapsarRepeticoes(desfazerLeet(bruta));
            if (ehOfensiva(palavra)) {
                ofensivas++;
            } else if (!DetectorDeIdioma.ehPalavraFuncional(palavra) && palavra.length() > 1) {
                conteudo++;
            }
        }
        return ofensivas > 0 && ofensivas * 2 >= palavras.size() && conteudo <= 2;
    }

    public static Set<String> introduzidos(String saida, String... fontes) {
        Set<String> naSaida = encontrar(saida);
        if (naSaida.isEmpty()) {
            return naSaida;
        }
        Set<String> nasFontes = new LinkedHashSet<>();
        for (String fonte : fontes) {
            nasFontes.addAll(encontrar(fonte));
        }
        naSaida.removeAll(nasFontes);
        return naSaida;
    }

    static boolean ehOfensiva(String palavra) {
        if (PALAVRAS.contains(palavra)) {
            return true;
        }
        for (String prefixo : PREFIXOS) {
            if (palavra.startsWith(prefixo)) {
                return true;
            }
        }
        return false;
    }

    private static String desfazerLeet(String palavra) {
        if (!palavra.matches(".*\\p{L}.*")) {
            return palavra;
        }
        return palavra.toLowerCase(Locale.ROOT)
                .replace('0', 'o').replace('1', 'i').replace('3', 'e').replace('4', 'a')
                .replace('5', 's').replace('7', 't').replace('@', 'a').replace('$', 's');
    }

    private static String colapsarRepeticoes(String palavra) {
        return palavra.replaceAll("(\\p{L})\\1{2,}", "$1");
    }
}
