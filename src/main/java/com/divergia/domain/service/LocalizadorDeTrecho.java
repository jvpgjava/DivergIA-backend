package com.divergia.domain.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LocalizadorDeTrecho {

    static final double COBERTURA_MINIMA = 0.85;
    private static final Pattern BORDAS = Pattern.compile("^[\\s\"'“”‘’«».…,;:()\\[\\]-]+|[\\s\"'“”‘’«».…,;:()\\[\\]-]+$");
    private static final Pattern RETICENCIAS = Pattern.compile("\\s*(?:\\.\\.\\.|…|\\[\\.\\.\\.\\]|\\(\\.\\.\\.\\))\\s*");
    private static final Pattern PALAVRA = Pattern.compile("[\\p{L}\\p{N}]+");

    private final String texto;
    private final StringBuilder normalizado = new StringBuilder();
    private final List<Integer> origem = new ArrayList<>();

    public LocalizadorDeTrecho(String texto) {
        this.texto = texto == null ? "" : texto;
        indexar();
    }

    public Optional<String> localizar(String citacao) {
        return localizarIntervalo(citacao).map(i -> texto.substring(i[0], i[1]));
    }

    public Optional<int[]> localizarIntervalo(String citacao) {
        if (citacao == null) {
            return Optional.empty();
        }
        String alvo = BORDAS.matcher(citacao.strip()).replaceAll("");
        if (alvo.isBlank()) {
            return Optional.empty();
        }
        Optional<int[]> exato = buscaNormalizada(alvo);
        if (exato.isPresent()) {
            return exato;
        }
        Optional<int[]> comReticencias = buscaComReticencias(alvo);
        if (comReticencias.isPresent()) {
            return comReticencias;
        }
        return buscaPorPalavras(alvo);
    }

    private Optional<int[]> buscaNormalizada(String alvo) {
        String agulha = normalizarSolto(alvo);
        if (agulha.isEmpty()) {
            return Optional.empty();
        }
        int k = normalizado.indexOf(agulha);
        if (k < 0) {
            return Optional.empty();
        }
        return Optional.of(paraOriginal(k, k + agulha.length()));
    }

    private Optional<int[]> buscaComReticencias(String alvo) {
        String[] partes = RETICENCIAS.split(alvo);
        if (partes.length < 2) {
            return Optional.empty();
        }
        int inicio = -1;
        int cursor = 0;
        int fim = -1;
        for (String parte : partes) {
            String agulha = normalizarSolto(BORDAS.matcher(parte).replaceAll(""));
            if (agulha.isEmpty()) {
                continue;
            }
            int k = normalizado.indexOf(agulha, cursor);
            if (k < 0) {
                return Optional.empty();
            }
            if (inicio < 0) {
                inicio = k;
            }
            cursor = k + agulha.length();
            fim = cursor;
        }
        if (inicio < 0) {
            return Optional.empty();
        }
        if (fim - inicio > alvo.length() * 3 + 200) {
            return Optional.empty();
        }
        return Optional.of(paraOriginal(inicio, fim));
    }

    private Optional<int[]> buscaPorPalavras(String alvo) {
        List<String> palavrasAlvo = new ArrayList<>();
        Matcher ma = PALAVRA.matcher(normalizarSolto(alvo));
        while (ma.find()) {
            palavrasAlvo.add(ma.group());
        }
        if (palavrasAlvo.size() < 3) {
            return Optional.empty();
        }
        List<int[]> spans = new ArrayList<>();
        List<String> palavrasTexto = new ArrayList<>();
        Matcher mt = PALAVRA.matcher(normalizado);
        while (mt.find()) {
            palavrasTexto.add(mt.group());
            spans.add(new int[] {mt.start(), mt.end()});
        }
        int n = palavrasAlvo.size();
        Map<String, Integer> contagemAlvo = contar(palavrasAlvo);
        double melhor = 0;
        int[] melhorJanela = null;
        for (int tamanho = Math.max(1, n - 2); tamanho <= n + 2; tamanho++) {
            for (int i = 0; i + tamanho <= palavrasTexto.size(); i++) {
                Map<String, Integer> janela = contar(palavrasTexto.subList(i, i + tamanho));
                int comuns = 0;
                for (Map.Entry<String, Integer> e : contagemAlvo.entrySet()) {
                    comuns += Math.min(e.getValue(), janela.getOrDefault(e.getKey(), 0));
                }
                double cobertura = (double) comuns / n;
                double precisao = (double) comuns / tamanho;
                double nota = Math.min(cobertura, precisao);
                if (nota > melhor) {
                    melhor = nota;
                    melhorJanela = new int[] {spans.get(i)[0], spans.get(i + tamanho - 1)[1]};
                }
            }
        }
        if (melhorJanela == null || melhor < COBERTURA_MINIMA) {
            return Optional.empty();
        }
        return Optional.of(paraOriginal(melhorJanela[0], melhorJanela[1]));
    }

    private void indexar() {
        boolean ultimoFoiEspaco = true;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (Character.isWhitespace(c) || c == ' ') {
                if (!ultimoFoiEspaco) {
                    normalizado.append(' ');
                    origem.add(i);
                    ultimoFoiEspaco = true;
                }
                continue;
            }
            String n = normalizarCaractere(c);
            for (int j = 0; j < n.length(); j++) {
                normalizado.append(n.charAt(j));
                origem.add(i);
            }
            if (!n.isEmpty()) {
                ultimoFoiEspaco = false;
            }
        }
    }

    private int[] paraOriginal(int inicioNorm, int fimNorm) {
        int inicio = origem.get(inicioNorm);
        int fim = origem.get(fimNorm - 1) + 1;
        return new int[] {inicio, fim};
    }

    private static String normalizarSolto(String s) {
        StringBuilder sb = new StringBuilder();
        boolean ultimoFoiEspaco = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c) || c == ' ') {
                if (!ultimoFoiEspaco) {
                    sb.append(' ');
                    ultimoFoiEspaco = true;
                }
                continue;
            }
            String n = normalizarCaractere(c);
            sb.append(n);
            if (!n.isEmpty()) {
                ultimoFoiEspaco = false;
            }
        }
        return sb.toString().strip();
    }

    private static String normalizarCaractere(char c) {
        switch (c) {
            case '“', '”', '„', '«', '»' -> {
                return "\"";
            }
            case '‘', '’', '`' -> {
                return "'";
            }
            case '–', '—' -> {
                return "-";
            }
            case '​', '‌', '‍', '﻿', '­' -> {
                return "";
            }
            default -> {
                String d = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
                d = d.replaceAll("\\p{M}+", "");
                return d.toLowerCase(Locale.ROOT);
            }
        }
    }

    private static Map<String, Integer> contar(List<String> palavras) {
        Map<String, Integer> m = new HashMap<>();
        for (String p : palavras) {
            m.merge(p, 1, Integer::sum);
        }
        return m;
    }
}
