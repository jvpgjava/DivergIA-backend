package com.divergia.adapter.out.llm;

import com.divergia.domain.model.AvaliacaoDeDeriva;
import com.divergia.domain.model.AvaliacaoDoModelo;
import com.divergia.domain.model.CampoEntrada;
import com.divergia.domain.model.MotivoEntradaInvalida;
import com.divergia.domain.model.TipoDesvio;
import com.divergia.domain.model.TriagemDaEntrada;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class RespostaDoModelo {
    private static final Set<MotivoEntradaInvalida> MOTIVOS_DO_MODELO = EnumSet.of(
            MotivoEntradaInvalida.NAO_E_TEXTO,
            MotivoEntradaInvalida.CONTEUDO_CODIFICADO,
            MotivoEntradaInvalida.TEXTO_SEM_SENTIDO,
            MotivoEntradaInvalida.INSTRUCAO_PARA_IA,
            MotivoEntradaInvalida.SO_OFENSA,
            MotivoEntradaInvalida.IDIOMAS_DIFERENTES,
            MotivoEntradaInvalida.SEM_RELACAO);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static final class ForaDoFormato extends RuntimeException {
        ForaDoFormato(String motivo) {
            super(motivo);
        }
    }

    record Analise(AvaliacaoDoModelo avaliacao, int itensMalformados) {
    }

    private RespostaDoModelo() {
    }

    static Analise analise(String resposta) {
        JsonNode raiz = json(resposta);
        JsonNode derivas;
        TriagemDaEntrada triagem = TriagemDaEntrada.valida();
        if (raiz.isArray()) {
            derivas = raiz;
        } else {
            derivas = raiz.get("derivas");
            if (derivas == null || !derivas.isArray()) {
                throw new ForaDoFormato("objeto sem a lista \"derivas\"");
            }
            triagem = triagem(raiz.get("triagem"));
        }
        List<AvaliacaoDeDeriva> lista = new ArrayList<>();
        int malformados = 0;
        for (JsonNode item : derivas) {
            AvaliacaoDeDeriva deriva = deriva(item);
            if (deriva == null) {
                malformados++;
            } else {
                lista.add(deriva);
            }
        }
        return new Analise(new AvaliacaoDoModelo(triagem, triagem.ehValida() ? lista : List.of()), malformados);
    }

    private static TriagemDaEntrada triagem(JsonNode no) {
        if (no == null || !no.isObject()) {
            return TriagemDaEntrada.valida();
        }
        String status = codigo(texto(no, "status"));
        MotivoEntradaInvalida motivo = null;
        if (status != null && !Set.of("VALIDO", "VALIDA", "OK").contains(status)) {
            try {
                MotivoEntradaInvalida candidato = MotivoEntradaInvalida.valueOf(status);
                motivo = MOTIVOS_DO_MODELO.contains(candidato) ? candidato : null;
            } catch (IllegalArgumentException e) {
                motivo = null;
            }
        }
        CampoEntrada campo = null;
        String campoTexto = codigo(texto(no, "campo"));
        if (campoTexto != null) {
            try {
                campo = CampoEntrada.valueOf(campoTexto);
            } catch (IllegalArgumentException e) {
                campo = CampoEntrada.AMBOS;
            }
        }
        double confianca = numero(no.get("confianca"), 1.0);
        return new TriagemDaEntrada(motivo, motivo == null ? null : campo,
                idioma(texto(no, "idiomaOriginal")), idioma(texto(no, "idiomaEditado")), confianca);
    }

    private static AvaliacaoDeDeriva deriva(JsonNode item) {
        if (item == null || !item.isObject()) {
            return null;
        }
        String tipo = codigo(texto(item, "tipoDesvio"));
        String original = texto(item, "trechoOriginal");
        String editado = texto(item, "trechoEditado");
        String explicacao = texto(item, "explicacao");
        if (tipo == null || original == null || editado == null || explicacao == null) {
            return null;
        }
        TipoDesvio tipoDesvio;
        try {
            tipoDesvio = TipoDesvio.valueOf(tipo);
        } catch (IllegalArgumentException e) {
            return null;
        }
        double intensidade = numero(item.get("intensidade"), Double.NaN);
        if (Double.isNaN(intensidade)) {
            return null;
        }
        // Alguns modelos respondem em 0–100 mesmo instruídos a usar 0–1.
        if (intensidade > 1.0 && intensidade <= 100.0) {
            intensidade = intensidade / 100.0;
        }
        intensidade = Math.max(0.0, Math.min(1.0, intensidade));
        try {
            return new AvaliacaoDeDeriva(tipoDesvio, original, editado, explicacao, intensidade);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static List<String> sugestoes(String resposta) {
        JsonNode raiz = json(resposta);
        JsonNode lista = raiz.isArray() ? raiz : raiz.get("sugestoes");
        if (lista == null || !lista.isArray()) {
            throw new ForaDoFormato("objeto sem a lista \"sugestoes\"");
        }
        List<String> sugestoes = new ArrayList<>();
        for (JsonNode item : lista) {
            if (item.isTextual() && !item.asText().isBlank()) {
                sugestoes.add(item.asText());
            } else if (item.isObject() && item.hasNonNull("texto")) {
                sugestoes.add(item.get("texto").asText());
            }
        }
        if (sugestoes.isEmpty()) {
            throw new ForaDoFormato("lista \"sugestoes\" sem nenhum texto");
        }
        return sugestoes;
    }

    static List<Boolean> vereditos(String resposta, int quantidade) {
        JsonNode raiz = json(resposta);
        JsonNode lista = raiz.isArray() ? raiz : raiz.get("vereditos");
        if (lista == null || !lista.isArray()) {
            throw new ForaDoFormato("objeto sem a lista \"vereditos\"");
        }
        Boolean[] resultado = new Boolean[quantidade];
        int posicao = 0;
        for (JsonNode item : lista) {
            int indice;
            Boolean fiel;
            if (item.isBoolean()) {
                indice = posicao;
                fiel = item.asBoolean();
            } else if (item.isObject() && item.has("fiel")) {
                indice = item.has("indice") ? item.get("indice").asInt(posicao + 1) - 1 : posicao;
                fiel = item.get("fiel").isBoolean() ? item.get("fiel").asBoolean() : null;
            } else {
                indice = -1;
                fiel = null;
            }
            if (indice >= 0 && indice < quantidade && fiel != null) {
                resultado[indice] = fiel;
            }
            posicao++;
        }
        for (Boolean b : resultado) {
            if (b == null) {
                return List.of();
            }
        }
        return List.of(resultado);
    }

    static JsonNode json(String resposta) {
        if (resposta == null || resposta.isBlank()) {
            throw new ForaDoFormato("resposta vazia");
        }
        String t = resposta.strip()
                .replaceAll("^```(?:json|JSON)?\\s*", "")
                .replaceAll("\\s*```$", "")
                .strip();
        JsonNode no = tentarLer(t);
        if (no == null) {
            int inicio = primeiroIndice(t, '{', '[');
            int fim = Math.max(t.lastIndexOf('}'), t.lastIndexOf(']'));
            if (inicio >= 0 && fim > inicio) {
                no = tentarLer(t.substring(inicio, fim + 1));
            }
        }
        if (no == null || !(no.isObject() || no.isArray())) {
            throw new ForaDoFormato("não é um JSON válido (tamanho " + resposta.length() + ")");
        }
        return no;
    }

    private static JsonNode tentarLer(String t) {
        try {
            return MAPPER.readTree(t);
        } catch (Exception e) {
            return null;
        }
    }

    private static int primeiroIndice(String t, char a, char b) {
        int ia = t.indexOf(a);
        int ib = t.indexOf(b);
        if (ia < 0) {
            return ib;
        }
        if (ib < 0) {
            return ia;
        }
        return Math.min(ia, ib);
    }

    private static String texto(JsonNode no, String campo) {
        JsonNode valor = no.get(campo);
        if (valor == null || valor.isNull()) {
            return null;
        }
        String s = valor.asText();
        return s == null || s.isBlank() ? null : s;
    }

    private static double numero(JsonNode valor, double padrao) {
        if (valor == null || valor.isNull()) {
            return padrao;
        }
        if (valor.isNumber()) {
            return valor.asDouble();
        }
        try {
            return Double.parseDouble(valor.asText().replace(',', '.').strip());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    static String codigo(String s) {
        if (s == null) {
            return null;
        }
        String t = Normalizer.normalize(s.strip(), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        t = t.toUpperCase(Locale.ROOT).replaceAll("[\\s-]+", "_");
        return t.isEmpty() ? null : t;
    }

    private static String idioma(String s) {
        if (s == null) {
            return null;
        }
        String t = s.strip().toLowerCase(Locale.ROOT);
        return t.length() >= 2 ? t.substring(0, 2) : t;
    }
}
