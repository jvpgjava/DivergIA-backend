package com.divergia.domain.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class GuardaDeSugestoes {

    public static final int MAXIMO_SUGESTOES = 3;

    private static final Pattern PREFIXO_LISTA = Pattern.compile(
            "^\\s*(?:(?:alternativa|op[cç][aã]o|sugest[aã]o|vers[aã]o|reescrita)\\s*\\d*\\s*[:.)-]\\s*"
                    + "|\\d+\\s*[.)-]\\s+|[-*•]\\s+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern ASPAS_EXTERNAS = Pattern.compile("^[\"“”'‘’«»]+|[\"“”'‘’«»]+$");
    private static final Pattern MARKDOWN = Pattern.compile("\\*\\*|__|`+");
    private static final Pattern METACOMENTARIO = Pattern.compile(
            "^(?:claro|aqui est[aã]o?|segue|seguem|certo|ok)\\b|\\b(?:alternativas?|reescritas?|sugest[oõ]es)\\s*:"
                    + "|\\b(?:mant[eé]m|preserva|fiel ao) (?:o )?(?:sentido|original)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public enum Descarte {
        VAZIA, COPIA_DO_EDITADO, DUPLICADA, LONGA_DEMAIS, OUTRO_IDIOMA, METACOMENTARIO, TERMO_INTERNO, OFENSA_INTRODUZIDA
    }

    public record Resultado(List<String> aprovadas, Map<Descarte, Integer> descartes) {

        public Resultado {
            aprovadas = List.copyOf(aprovadas);
            descartes = Map.copyOf(descartes);
        }
    }

    public Resultado filtrar(String trechoOriginal, String trechoEditado, List<String> candidatas) {
        Map<Descarte, Integer> descartes = new LinkedHashMap<>();
        List<String> aprovadas = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
        Optional<String> idiomaOriginal = DetectorDeIdioma.detectar(trechoOriginal);
        int limiteTamanho = Math.max(trechoOriginal.length(), trechoEditado.length()) * 5 / 2 + 80;

        for (String bruta : candidatas == null ? List.<String>of() : candidatas) {
            String s = limpar(bruta);
            Descarte motivo = motivoDeDescarte(s, trechoOriginal, trechoEditado, idiomaOriginal, limiteTamanho, vistas);
            if (motivo != null) {
                descartes.merge(motivo, 1, Integer::sum);
                continue;
            }
            vistas.add(NormalizadorDeTexto.paraComparacao(s));
            if (aprovadas.size() < MAXIMO_SUGESTOES) {
                aprovadas.add(s);
            }
        }
        return new Resultado(aprovadas, descartes);
    }

    private static Descarte motivoDeDescarte(
            String s, String trechoOriginal, String trechoEditado, Optional<String> idiomaOriginal,
            int limiteTamanho, Set<String> vistas) {
        if (s.isBlank() || NormalizadorDeTexto.palavras(s).isEmpty()) {
            return Descarte.VAZIA;
        }
        if (NormalizadorDeTexto.identicos(s, trechoEditado)) {
            return Descarte.COPIA_DO_EDITADO;
        }
        if (vistas.contains(NormalizadorDeTexto.paraComparacao(s))) {
            return Descarte.DUPLICADA;
        }
        if (s.length() > limiteTamanho) {
            return Descarte.LONGA_DEMAIS;
        }
        java.util.regex.Matcher meta = METACOMENTARIO.matcher(s);
        if (meta.find() && !NormalizadorDeTexto.paraComparacao(trechoOriginal)
                .contains(NormalizadorDeTexto.paraComparacao(meta.group()))) {
            return Descarte.METACOMENTARIO;
        }
        if (GuardaDeDerivas.temTermoInternoAlheio(s, trechoOriginal, trechoEditado)) {
            return Descarte.TERMO_INTERNO;
        }
        if (!LexicoOfensivo.introduzidos(s, trechoOriginal).isEmpty()) {
            return Descarte.OFENSA_INTRODUZIDA;
        }
        Optional<String> idioma = DetectorDeIdioma.detectar(s);
        if (idiomaOriginal.isPresent() && idioma.isPresent() && !idiomaOriginal.get().equals(idioma.get())) {
            return Descarte.OUTRO_IDIOMA;
        }
        return null;
    }

    static String limpar(String bruta) {
        if (bruta == null) {
            return "";
        }
        String s = NormalizadorDeTexto.limpar(bruta);
        s = MARKDOWN.matcher(s).replaceAll("");
        s = PREFIXO_LISTA.matcher(s).replaceFirst("");
        s = ASPAS_EXTERNAS.matcher(s.strip()).replaceAll("");
        return s.strip();
    }
}
