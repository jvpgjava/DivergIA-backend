package com.divergia.domain.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DetectorDeIdioma {
    static final int MINIMO_PALAVRAS_FUNCIONAIS = 4;
    static final double VANTAGEM_MINIMA = 0.6;

    private static final Map<String, Set<String>> FUNCIONAIS = new LinkedHashMap<>();

    static {
        FUNCIONAIS.put("pt", Set.of(
                "o", "os", "as", "um", "uma", "uns", "umas", "do", "da", "dos", "das", "na", "nas",
                "nos", "ao", "aos", "pela", "pelo", "pelas", "pelos", "com", "para", "pra", "nao",
                "sao", "voce", "voces", "isso", "isto", "esse", "essa", "este", "esta", "eu", "ele",
                "ela", "eles", "elas", "mais", "muito", "muita", "tambem", "entao", "mas", "ou",
                "seu", "sua", "seus", "suas", "meu", "minha", "foi", "ser", "tem", "ja", "quando",
                "onde", "porque", "como", "e", "num", "numa", "sem", "ate", "agora"));
        FUNCIONAIS.put("es", Set.of(
                "el", "los", "las", "un", "una", "unos", "unas", "del", "al", "con", "para", "por",
                "pero", "muy", "tambien", "esto", "eso", "este", "esta", "es", "son", "yo", "usted",
                "ustedes", "ellos", "ellas", "nosotros", "su", "sus", "mi", "fue", "ser", "tiene",
                "ya", "cuando", "donde", "porque", "como", "y", "sin", "hasta", "ahora", "hay", "lo"));
        FUNCIONAIS.put("en", Set.of(
                "the", "an", "of", "to", "in", "on", "and", "or", "but", "is", "are", "was",
                "were", "be", "been", "it", "its", "this", "that", "these", "those", "with", "for",
                "from", "by", "not", "you", "your", "we", "they", "he", "she", "i", "my", "our",
                "their", "have", "has", "had", "will", "would", "can", "could", "should", "very",
                "also", "when", "where", "because", "what", "which", "who"));
        FUNCIONAIS.put("fr", Set.of(
                "le", "les", "un", "une", "des", "du", "au", "aux", "et", "ou", "mais", "est", "sont",
                "ce", "cette", "ces", "avec", "pour", "dans", "sur", "pas", "ne", "je", "tu", "il",
                "elle", "nous", "vous", "ils", "elles", "mon", "ma", "mes", "son", "sa", "leur",
                "tres", "aussi", "quand", "parce", "qui", "quoi", "etait", "avoir", "etre"));
        FUNCIONAIS.put("it", Set.of(
                "il", "lo", "gli", "le", "un", "uno", "una", "del", "della", "dei", "degli", "delle",
                "al", "alla", "nel", "nella", "con", "per", "ma", "non", "sono", "questo", "questa",
                "quello", "quella", "io", "lui", "lei", "noi", "voi", "loro", "mio", "mia", "molto",
                "anche", "quando", "dove", "perche", "come", "ed", "senza", "ancora", "ha", "hanno"));
        FUNCIONAIS.put("de", Set.of(
                "der", "die", "das", "den", "dem", "des", "ein", "eine", "einen", "einem", "und",
                "oder", "aber", "ist", "sind", "war", "nicht", "mit", "fur", "auf", "aus", "bei",
                "ich", "du", "er", "sie", "es", "wir", "ihr", "mein", "dein", "sehr", "auch", "wenn",
                "wo", "weil", "wie", "was", "wer", "haben", "hat", "werden", "zu", "im", "vom"));
    }

    private static final Map<String, String> NOMES = Map.of(
            "pt", "português", "es", "espanhol", "en", "inglês",
            "fr", "francês", "it", "italiano", "de", "alemão");

    private DetectorDeIdioma() {
    }

    public static Optional<String> detectar(String texto) {
        List<String> palavras = NormalizadorDeTexto.palavras(texto);
        Map<String, Integer> pontos = new HashMap<>();
        int total = 0;
        for (String palavra : palavras) {
            boolean contou = false;
            for (Map.Entry<String, Set<String>> idioma : FUNCIONAIS.entrySet()) {
                if (idioma.getValue().contains(palavra)) {
                    pontos.merge(idioma.getKey(), 1, Integer::sum);
                    contou = true;
                }
            }
            if (contou) {
                total++;
            }
        }
        if (total < MINIMO_PALAVRAS_FUNCIONAIS) {
            return Optional.empty();
        }
        String melhor = null;
        int melhorPontos = 0;
        int segundoPontos = 0;
        for (Map.Entry<String, Integer> e : pontos.entrySet()) {
            if (e.getValue() > melhorPontos) {
                segundoPontos = melhorPontos;
                melhorPontos = e.getValue();
                melhor = e.getKey();
            } else if (e.getValue() > segundoPontos) {
                segundoPontos = e.getValue();
            }
        }
        if (melhor == null || melhorPontos < MINIMO_PALAVRAS_FUNCIONAIS) {
            return Optional.empty();
        }
        double vantagem = (double) melhorPontos / (melhorPontos + segundoPontos);
        return vantagem >= VANTAGEM_MINIMA ? Optional.of(melhor) : Optional.empty();
    }

    public static boolean ehPalavraFuncional(String palavra) {
        for (Set<String> lista : FUNCIONAIS.values()) {
            if (lista.contains(palavra)) {
                return true;
            }
        }
        return false;
    }

    public static String nome(String codigo) {
        if (codigo == null) {
            return "desconhecido";
        }
        return NOMES.getOrDefault(codigo.toLowerCase(), codigo);
    }
}
