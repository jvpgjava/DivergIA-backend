package com.divergia.domain.service;

import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.TipoDesvio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class GuardaDeDerivas {

    public static final double INTENSIDADE_MINIMA = 0.05;
    public static final int MAXIMO_DERIVAS = 12;
    static final int MAXIMO_EXPLICACAO = 600;
    static final int MINIMO_PARA_CHECAR_INALTERADO = 15;

    static final Pattern TERMOS_INTERNOS = Pattern.compile(
            "\\bprompt\\b|instru[cç](?:[aã]o|[oõ]es) (?:do sistema|recebidas?|internas?)|\\bsystem\\b"
                    + "|\\bRAG\\b|exemplos? de refer[eê]ncia|base (?:de|vetorial) (?:dados|exemplos|conhecimento)"
                    + "|\\bembedding|\\bJSON\\b|\\barray\\b|<\\/?texto_|\\[T\\d+\\]|https?://|www\\."
                    + "|como (?:um|uma) (?:IA|intelig[eê]ncia artificial|modelo de linguagem)"
                    + "|sou (?:um|uma) (?:IA|assistente|modelo)|n[aã]o (?:posso|consigo) (?:ajudar|analisar|responder)"
                    + "|\\bClaude\\b|\\bAnthropic\\b|\\bGPT\\b|\\bOpenAI\\b|\\bAbacus\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern CITACAO = Pattern.compile("[\"“”']([^\"“”']{3,})[\"“”']");
    private static final Pattern MARKDOWN = Pattern.compile("\\*\\*|__|`+|^#+\\s*|^\\s*[-*]\\s+", Pattern.MULTILINE);

    public enum Descarte {
        CITACAO_ORIGINAL_INEXISTENTE,
        CITACAO_EDITADA_INEXISTENTE,
        SEM_MUDANCA,
        TRECHO_INALTERADO,
        INTENSIDADE_IRRELEVANTE,
        SOBREPOSTA,
        EXCEDENTE
    }

    public record Resultado(List<AvaliacaoDeDeriva> aprovadas, Map<Descarte, Integer> descartes, int explicacoesTrocadas) {

        public Resultado {
            aprovadas = List.copyOf(aprovadas);
            descartes = Map.copyOf(descartes);
        }

        public int totalDescartado() {
            return descartes.values().stream().mapToInt(Integer::intValue).sum();
        }
    }

    private record Candidata(AvaliacaoDeDeriva deriva, int inicioEditado, int fimEditado) {
    }

    public Resultado filtrar(String textoOriginal, String textoEditado, List<AvaliacaoDeDeriva> candidatas) {
        LocalizadorDeTrecho noOriginal = new LocalizadorDeTrecho(textoOriginal);
        LocalizadorDeTrecho noEditado = new LocalizadorDeTrecho(textoEditado);
        String originalComparavel = NormalizadorDeTexto.paraComparacao(textoOriginal);
        Map<Descarte, Integer> descartes = new LinkedHashMap<>();
        List<Candidata> aceitas = new ArrayList<>();
        int explicacoesTrocadas = 0;

        for (AvaliacaoDeDeriva deriva : candidatas == null ? List.<AvaliacaoDeDeriva>of() : candidatas) {
            // Citação que não existe no texto do usuário é alucinação: a deriva é descartada.
            Optional<int[]> intervaloOriginal = noOriginal.localizarIntervalo(deriva.trechoOriginal());
            if (intervaloOriginal.isEmpty()) {
                contar(descartes, Descarte.CITACAO_ORIGINAL_INEXISTENTE);
                continue;
            }
            Optional<int[]> intervaloEditado = noEditado.localizarIntervalo(deriva.trechoEditado());
            if (intervaloEditado.isEmpty()) {
                contar(descartes, Descarte.CITACAO_EDITADA_INEXISTENTE);
                continue;
            }
            String trechoOriginal = textoOriginal.substring(intervaloOriginal.get()[0], intervaloOriginal.get()[1]);
            String trechoEditado = textoEditado.substring(intervaloEditado.get()[0], intervaloEditado.get()[1]);

            if (NormalizadorDeTexto.identicos(trechoOriginal, trechoEditado)) {
                contar(descartes, Descarte.SEM_MUDANCA);
                continue;
            }
            String editadoComparavel = NormalizadorDeTexto.paraComparacao(trechoEditado);
            if (editadoComparavel.length() >= MINIMO_PARA_CHECAR_INALTERADO
                    && originalComparavel.contains(editadoComparavel)) {
                contar(descartes, Descarte.TRECHO_INALTERADO);
                continue;
            }
            if (deriva.intensidade() < INTENSIDADE_MINIMA) {
                contar(descartes, Descarte.INTENSIDADE_IRRELEVANTE);
                continue;
            }

            String explicacao = limparExplicacao(deriva.explicacao());
            if (!explicacaoAceitavel(explicacao, textoOriginal, textoEditado)) {
                explicacao = explicacaoPadrao(deriva.tipoDesvio(), trechoOriginal, trechoEditado);
                explicacoesTrocadas++;
            }

            AvaliacaoDeDeriva ancorada = new AvaliacaoDeDeriva(
                    deriva.tipoDesvio(), trechoOriginal, trechoEditado, explicacao, deriva.intensidade());
            aceitas.add(new Candidata(ancorada, intervaloEditado.get()[0], intervaloEditado.get()[1]));
        }

        List<Candidata> semSobreposicao = removerSobrepostas(aceitas, descartes);
        semSobreposicao.sort(Comparator.comparingDouble((Candidata c) -> c.deriva().intensidade()).reversed());
        if (semSobreposicao.size() > MAXIMO_DERIVAS) {
            for (int i = MAXIMO_DERIVAS; i < semSobreposicao.size(); i++) {
                contar(descartes, Descarte.EXCEDENTE);
            }
            semSobreposicao = new ArrayList<>(semSobreposicao.subList(0, MAXIMO_DERIVAS));
        }
        semSobreposicao.sort(Comparator.comparingInt(Candidata::inicioEditado));
        return new Resultado(
                semSobreposicao.stream().map(Candidata::deriva).toList(), descartes, explicacoesTrocadas);
    }

    private static List<Candidata> removerSobrepostas(List<Candidata> aceitas, Map<Descarte, Integer> descartes) {
        List<Candidata> ordenadas = new ArrayList<>(aceitas);
        ordenadas.sort(Comparator.comparingDouble((Candidata c) -> c.deriva().intensidade()).reversed());
        List<Candidata> mantidas = new ArrayList<>();
        for (Candidata c : ordenadas) {
            boolean sobrepoe = mantidas.stream().anyMatch(m -> sobrepoe(m, c));
            if (sobrepoe) {
                contar(descartes, Descarte.SOBREPOSTA);
            } else {
                mantidas.add(c);
            }
        }
        return mantidas;
    }

    private static boolean sobrepoe(Candidata a, Candidata b) {
        int inicio = Math.max(a.inicioEditado(), b.inicioEditado());
        int fim = Math.min(a.fimEditado(), b.fimEditado());
        int comum = fim - inicio;
        if (comum <= 0) {
            return false;
        }
        int menor = Math.min(a.fimEditado() - a.inicioEditado(), b.fimEditado() - b.inicioEditado());
        return comum * 2 >= menor;
    }

    static String limparExplicacao(String explicacao) {
        if (explicacao == null) {
            return "";
        }
        String t = MARKDOWN.matcher(explicacao).replaceAll("");
        t = t.replaceAll("\\s+", " ").strip();
        if (t.length() > MAXIMO_EXPLICACAO) {
            int corte = t.lastIndexOf(". ", MAXIMO_EXPLICACAO);
            t = corte > MAXIMO_EXPLICACAO / 2 ? t.substring(0, corte + 1) : t.substring(0, MAXIMO_EXPLICACAO).strip() + "…";
        }
        return t;
    }

    static boolean explicacaoAceitavel(String explicacao, String textoOriginal, String textoEditado) {
        if (explicacao == null || explicacao.length() < 10) {
            return false;
        }
        if (temTermoInternoAlheio(explicacao, textoOriginal, textoEditado)) {
            return false;
        }
        if (temCitacaoAlheia(explicacao, textoOriginal, textoEditado)) {
            return false;
        }
        return LexicoOfensivo.introduzidos(explicacao, textoOriginal, textoEditado).isEmpty();
    }

    // Citação na explicação que não está nos textos do usuário pode vir dos exemplos do RAG,
    // que são textos de outras pessoas: não pode chegar à tela.
    static boolean temCitacaoAlheia(String explicacao, String textoOriginal, String textoEditado) {
        String fontes = NormalizadorDeTexto.paraComparacao(textoOriginal) + " "
                + NormalizadorDeTexto.paraComparacao(textoEditado);
        java.util.regex.Matcher m = CITACAO.matcher(explicacao);
        while (m.find()) {
            String citado = NormalizadorDeTexto.paraComparacao(m.group(1));
            if (NormalizadorDeTexto.palavras(citado).size() >= 3 && !fontes.contains(citado)) {
                return true;
            }
        }
        return false;
    }

    static boolean temTermoInternoAlheio(String saida, String... fontes) {
        java.util.regex.Matcher m = TERMOS_INTERNOS.matcher(saida);
        while (m.find()) {
            String termo = NormalizadorDeTexto.paraComparacao(m.group());
            boolean veioDoUsuario = false;
            for (String fonte : fontes) {
                if (fonte != null && NormalizadorDeTexto.paraComparacao(fonte).contains(termo)) {
                    veioDoUsuario = true;
                    break;
                }
            }
            if (!veioDoUsuario) {
                return true;
            }
        }
        return false;
    }

    static String explicacaoPadrao(TipoDesvio tipo, String trechoOriginal, String trechoEditado) {
        String o = encurtar(trechoOriginal);
        String e = encurtar(trechoEditado);
        return switch (tipo) {
            case SENTIDO -> "A versão editada muda a ideia do original: “" + o + "” virou “" + e + "”.";
            case POSICAO -> "A versão editada altera a posição/opinião expressa no original: “" + o
                    + "” virou “" + e + "”.";
            case INTENSIDADE -> "A versão editada muda a força da afirmação original: “" + o + "” virou “" + e + "”.";
        };
    }

    private static String encurtar(String s) {
        String t = s.replaceAll("\\s+", " ").strip();
        return t.length() <= 120 ? t : t.substring(0, 117).strip() + "…";
    }

    private static void contar(Map<Descarte, Integer> descartes, Descarte motivo) {
        descartes.merge(motivo, 1, Integer::sum);
    }
}
