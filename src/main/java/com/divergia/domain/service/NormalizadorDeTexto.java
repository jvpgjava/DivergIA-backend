package com.divergia.domain.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NormalizadorDeTexto {
    private static final Pattern INVISIVEIS = Pattern.compile(
            "[\\u200B-\\u200F\\u202A-\\u202E\\u2060-\\u2064\\uFEFF\\u00AD]|[\\x{E0000}-\\x{E007F}]");
    private static final Pattern CONTROLE = Pattern.compile("[\\p{Cc}&&[^\\n\\t]]");
    private static final Pattern ESPACOS_HORIZONTAIS = Pattern.compile("[ \\t\\u00A0\\u2000-\\u200A\\u202F\\u205F\\u3000]+");
    private static final Pattern MUITAS_QUEBRAS = Pattern.compile("\\n{3,}");
    private static final Pattern DIACRITICOS = Pattern.compile("\\p{M}+");
    private static final Pattern PALAVRA = Pattern.compile("[\\p{L}\\p{N}]+");

    private NormalizadorDeTexto() {
    }

    public static int contarInvisiveis(String texto) {
        if (texto == null) {
            return 0;
        }
        Matcher m = INVISIVEIS.matcher(texto);
        int n = 0;
        while (m.find()) {
            n++;
        }
        return n;
    }

    public static String limpar(String texto) {
        if (texto == null) {
            return "";
        }
        String t = Normalizer.normalize(texto, Normalizer.Form.NFC);
        t = t.replace("\r\n", "\n").replace('\r', '\n');
        t = INVISIVEIS.matcher(t).replaceAll("");
        t = CONTROLE.matcher(t).replaceAll("");
        t = ESPACOS_HORIZONTAIS.matcher(t).replaceAll(" ");
        t = t.lines().map(String::strip).reduce((a, b) -> a + "\n" + b).orElse("");
        t = MUITAS_QUEBRAS.matcher(t).replaceAll("\n\n");
        return t.strip();
    }

    public static String paraComparacao(String texto) {
        if (texto == null) {
            return "";
        }
        String t = Normalizer.normalize(limpar(texto), Normalizer.Form.NFD);
        t = DIACRITICOS.matcher(t).replaceAll("");
        t = t.toLowerCase(Locale.ROOT)
                .replace('“', '"').replace('”', '"').replace('„', '"').replace('«', '"').replace('»', '"')
                .replace('‘', '\'').replace('’', '\'').replace('`', '\'')
                .replace('–', '-').replace('—', '-')
                .replace("…", "...");
        return t.replaceAll("\\s+", " ").strip();
    }

    public static List<String> palavras(String texto) {
        List<String> resultado = new ArrayList<>();
        Matcher m = PALAVRA.matcher(paraComparacao(texto));
        while (m.find()) {
            resultado.add(m.group());
        }
        return resultado;
    }

    private static final Pattern PALAVRA_COM_LEET = Pattern.compile("[\\p{L}\\p{N}@$]+");

    public static List<String> palavrasComLeet(String texto) {
        List<String> resultado = new ArrayList<>();
        Matcher m = PALAVRA_COM_LEET.matcher(paraComparacao(texto));
        while (m.find()) {
            resultado.add(m.group());
        }
        return resultado;
    }

    // Pontuação e acentos contam ("Ele veio." ≠ "Ele veio?"): só espaços são ignorados.
    public static boolean identicos(String a, String b) {
        return limpar(a).replaceAll("\\s+", " ").equals(limpar(b).replaceAll("\\s+", " "));
    }
}
