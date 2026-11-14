package com.divergia.domain.service;

import com.divergia.domain.model.CampoEntrada;
import com.divergia.domain.model.MotivoEntradaInvalida;
import com.divergia.domain.model.ProblemaDeEntrada;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValidadorDeEntrada {

    public static final int MINIMO_PALAVRAS = 2;

    private static final Pattern BINARIO = Pattern.compile("(?:\\b[01]{8}\\b[\\s,]*){4,}");
    private static final Pattern SO_BINARIO = Pattern.compile("^[01\\s,]{16,}$");
    private static final Pattern HEX = Pattern.compile(
            "(?:\\b[0-9a-fA-F]{2}\\b[\\s,:]*){16,}|\\b(?:0x)?[0-9a-fA-F]{32,}\\b"
                    + "|\\b0x[0-9a-fA-F]{2}(?:[\\s,]+0x[0-9a-fA-F]{2}){7,}");
    private static final Pattern BASE64 = Pattern.compile("[A-Za-z0-9+/_-]{40,}={0,2}");
    private static final Pattern VOGAL = Pattern.compile("[aeiouyáéíóúâêôãõàü]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern CONSOANTES_SEGUIDAS = Pattern.compile(
            "[bcdfghjklmnpqrstvwxz]{5,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern MESMO_CARACTERE = Pattern.compile("(.)\\1{5,}");
    private static final Pattern PALAVRA_DE_LETRAS = Pattern.compile("\\p{L}+");

    private static final List<Pattern> INJECAO = List.of(
            Pattern.compile("\\b(?:ignor\\w*|esquec\\w*|desconsider\\w*|descart\\w*|desobedec\\w*)\\b(?:\\s+\\w+){0,4}?\\s+"
                    + "(?:instruc\\w*|regras?|ordens|diretrizes|restric\\w*|prompt|comandos)"),
            Pattern.compile("\\b(?:ignore|disregard|forget)\\b(?:\\s+\\w+){0,4}?\\s+(?:instructions?|rules?|prompt|guidelines)"),
            Pattern.compile("\\bvoce agora (?:e|sera)\\b|\\ba partir de agora,? voce (?:e|sera|vai ser|nao tem)"),
            Pattern.compile("\\byou are now\\b|\\bpretend (?:to be|you)\\b|\\bact as (?:if|an? (?:unrestricted|unfiltered))"),
            Pattern.compile("\\bsystem prompt\\b|\\bprompt (?:do|de) sistema\\b"),
            Pattern.compile("\\b(?:mostr\\w*|revel\\w*|repit\\w*|imprim\\w*|reveal|show|print|repeat)\\b(?:\\s+\\w+){0,3}?\\s+"
                    + "(?:(?:suas?|seus?|your)\\s+(?:instruc\\w*|regras|prompt|diretrizes)|(?:o\\s+)?prompt\\b|instructions\\b)"),
            Pattern.compile("\\bdo anything now\\b|\\bjailbreak\\w*|\\bdeveloper mode\\b|\\bmodo (?:desenvolvedor|dev|deus|sem filtros?)\\b"),
            Pattern.compile("\\b(?:responda|retorne|devolva|diga|answer|respond|return)\\b(?:\\s+\\w+){0,4}?\\s+"
                    + "(?:que nao ha (?:deriva|divergencia)|array vazio|\\[\\]|nenhuma (?:deriva|divergencia)|no (?:drift|changes))"),
            Pattern.compile("</?\\s*(?:system|instructions?|prompt|texto_original|texto_editado)\\s*>|\\[/?(?:inst|system)\\]|<\\|im_start\\|>"));
    private static final Pattern ROTULO_DE_PAPEL = Pattern.compile(
            "(?:^|[.!?\\n])\\s*(?:system|assistant|developer)\\s*:", Pattern.CASE_INSENSITIVE);

    static final int LIMITE_INJECAO_DOMINANTE = 400;

    private final int maximoCaracteres;

    public ValidadorDeEntrada(int maximoCaracteres) {
        if (maximoCaracteres <= 0) {
            throw new IllegalArgumentException("maximoCaracteres deve ser positivo");
        }
        this.maximoCaracteres = maximoCaracteres;
    }

    public int maximoCaracteres() {
        return maximoCaracteres;
    }

    public Optional<ProblemaDeEntrada> validarLado(String texto, CampoEntrada campo, boolean deArquivo) {
        String t = texto == null ? "" : texto.strip();
        if (t.isEmpty()) {
            return problema(deArquivo ? MotivoEntradaInvalida.ARQUIVO_SEM_TEXTO : MotivoEntradaInvalida.TEXTO_VAZIO, campo);
        }
        if (t.length() > maximoCaracteres) {
            return Optional.of(ProblemaDeEntrada.textoLongo(campo, maximoCaracteres));
        }
        if (ehCodificado(t)) {
            return problema(MotivoEntradaInvalida.CONTEUDO_CODIFICADO, campo);
        }
        if (naoEhTexto(t)) {
            return problema(MotivoEntradaInvalida.NAO_E_TEXTO, campo);
        }
        if (letraRepetidaDomina(t)) {
            return problema(MotivoEntradaInvalida.TEXTO_SEM_SENTIDO, campo);
        }
        List<String> palavras = palavrasDeLetras(t);
        if (palavras.size() < MINIMO_PALAVRAS) {
            return problema(MotivoEntradaInvalida.TEXTO_CURTO, campo);
        }
        if (semSentido(t, palavras)) {
            return problema(MotivoEntradaInvalida.TEXTO_SEM_SENTIDO, campo);
        }
        if (instrucaoDominante(t)) {
            return problema(MotivoEntradaInvalida.INSTRUCAO_PARA_IA, campo);
        }
        if (LexicoOfensivo.soOfensa(t)) {
            return problema(MotivoEntradaInvalida.SO_OFENSA, campo);
        }
        return Optional.empty();
    }

    public Optional<ProblemaDeEntrada> validarPar(String original, String editado) {
        Optional<String> idiomaOriginal = DetectorDeIdioma.detectar(original);
        Optional<String> idiomaEditado = DetectorDeIdioma.detectar(editado);
        if (idiomaOriginal.isPresent() && idiomaEditado.isPresent()
                && !idiomaOriginal.get().equals(idiomaEditado.get())) {
            return Optional.of(ProblemaDeEntrada.idiomasDiferentes(
                    DetectorDeIdioma.nome(idiomaOriginal.get()), DetectorDeIdioma.nome(idiomaEditado.get())));
        }
        return Optional.empty();
    }

    static boolean ehCodificado(String t) {
        if (SO_BINARIO.matcher(t).matches() || BINARIO.matcher(t).find() || HEX.matcher(t).find()) {
            return true;
        }
        Matcher m = BASE64.matcher(t);
        while (m.find()) {
            if (decodificaParaTexto(m.group())) {
                return true;
            }
        }
        return false;
    }

    static boolean naoEhTexto(String t) {
        int visiveis = 0;
        int letras = 0;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (!Character.isWhitespace(c)) {
                visiveis++;
                if (Character.isLetter(c)) {
                    letras++;
                }
            }
        }
        return visiveis == 0 || letras < visiveis * 0.5;
    }

    static boolean letraRepetidaDomina(String t) {
        String semEspacos = t.replaceAll("\\s+", "");
        Matcher repetido = MESMO_CARACTERE.matcher(semEspacos);
        return repetido.find() && repetido.group().length() * 2 >= semEspacos.length();
    }

    static boolean semSentido(String t, List<String> palavras) {
        int longas = 0;
        int estranhas = 0;
        for (String palavra : palavras) {
            if (palavra.length() >= 4 && !palavra.equals(palavra.toUpperCase())) {
                longas++;
                if (!VOGAL.matcher(palavra).find() || CONSOANTES_SEGUIDAS.matcher(palavra).find()) {
                    estranhas++;
                }
            }
        }
        if (longas > 0 && estranhas * 10 >= longas * 4) {
            return true;
        }
        if (palavras.size() >= 6) {
            Set<String> distintas = new HashSet<>();
            for (String palavra : palavras) {
                distintas.add(palavra.toLowerCase());
            }
            return distintas.size() * 10 < palavras.size() * 3;
        }
        return false;
    }

    static boolean instrucaoDominante(String t) {
        String norm = NormalizadorDeTexto.paraComparacao(t);
        boolean temMarcador = INJECAO.stream().anyMatch(p -> p.matcher(norm).find());
        boolean temRotulo = ROTULO_DE_PAPEL.matcher(t).find();
        if (!temMarcador && !temRotulo) {
            return false;
        }
        boolean tagDoPrompt = INJECAO.get(INJECAO.size() - 1).matcher(norm).find();
        // Em texto longo o marcador vira dado: um artigo sobre IA não pode ser recusado.
        return tagDoPrompt || t.length() <= LIMITE_INJECAO_DOMINANTE;
    }

    private static List<String> palavrasDeLetras(String t) {
        Matcher m = PALAVRA_DE_LETRAS.matcher(t);
        List<String> palavras = new java.util.ArrayList<>();
        while (m.find()) {
            palavras.add(m.group());
        }
        return palavras;
    }

    private static boolean decodificaParaTexto(String candidato) {
        String ajustado = candidato.replace('-', '+').replace('_', '/');
        ajustado = ajustado + "=".repeat((4 - ajustado.length() % 4) % 4);
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(ajustado);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (bytes.length < 24) {
            return false;
        }
        String decodificado;
        try {
            decodificado = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return false;
        }
        long imprimiveis = decodificado.chars()
                .filter(c -> !Character.isISOControl(c) || c == '\n' || c == '\t').count();
        return imprimiveis >= decodificado.length() * 0.95;
    }

    private static Optional<ProblemaDeEntrada> problema(MotivoEntradaInvalida motivo, CampoEntrada campo) {
        return Optional.of(ProblemaDeEntrada.de(motivo, campo));
    }
}
