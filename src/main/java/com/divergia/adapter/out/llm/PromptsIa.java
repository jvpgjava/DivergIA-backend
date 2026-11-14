package com.divergia.adapter.out.llm;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public final class PromptsIa {

    private final String analise;
    private final String reescrita;
    private final String juizFidelidade;

    private PromptsIa(String analise, String reescrita, String juizFidelidade) {
        this.analise = analise;
        this.reescrita = reescrita;
        this.juizFidelidade = juizFidelidade;
    }

    public static PromptsIa carregar() {
        return new PromptsIa(ler("analise"), ler("reescrita"), ler("juiz-fidelidade"));
    }

    public String analise() {
        return analise;
    }

    public String reescrita() {
        return reescrita;
    }

    public String juizFidelidade() {
        return juizFidelidade;
    }

    private static String ler(String tarefa) {
        try {
            Resource[] arquivos = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:prompts/" + tarefa + "/*.md");
            if (arquivos.length == 0) {
                throw new IllegalStateException("Nenhum prompt encontrado em classpath:prompts/" + tarefa + "/");
            }
            return Arrays.stream(arquivos)
                    .sorted(Comparator.comparing(r -> String.valueOf(r.getFilename())))
                    .map(PromptsIa::conteudo)
                    .collect(Collectors.joining("\n\n"));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao carregar os prompts de " + tarefa, e);
        }
    }

    private static String conteudo(Resource recurso) {
        try {
            return new String(recurso.getContentAsByteArray(), StandardCharsets.UTF_8).strip();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o prompt " + recurso.getFilename(), e);
        }
    }
}
