package com.divergia.domain.model;

public enum MotivoEntradaInvalida {

    TEXTO_VAZIO("O %s está vazio. Cole o texto ou anexe um arquivo."),
    ARQUIVO_SEM_TEXTO("Não encontramos texto legível no arquivo do %s. Se for um PDF escaneado (imagem), "
            + "cole o texto diretamente."),
    TEXTO_CURTO("O %s é curto demais para comparar. Use pelo menos uma frase com algumas palavras."),
    TEXTO_LONGO("O %s passou do limite de %d caracteres. Divida o texto em partes menores."),
    NAO_E_TEXTO("O %s não parece ser um texto escrito (só números, símbolos ou código). "
            + "O DivergIA compara frases em linguagem natural."),
    CONTEUDO_CODIFICADO("O %s tem conteúdo codificado (binário, hexadecimal ou base64). "
            + "Cole o texto em linguagem natural."),
    TEXTO_SEM_SENTIDO("O %s não parece ter palavras com sentido. Confira se colou o texto certo."),
    INSTRUCAO_PARA_IA("O %s parece ser uma instrução para a IA, não um texto para comparar. "
            + "Cole apenas o texto que você escreveu e a versão devolvida pela IA."),
    SO_OFENSA("O %s tem só ofensas, sem uma mensagem para comparar. "
            + "O DivergIA analisa textos com conteúdo."),
    IDIOMAS_DIFERENTES("Os textos estão em idiomas diferentes (%s no original e %s no editado). "
            + "Isso é uma tradução, não uma edição: o DivergIA compara duas versões no mesmo idioma."),
    SEM_RELACAO("O texto editado não parece ser uma versão do original: os dois tratam de assuntos "
            + "diferentes. Confira se colou os textos certos.");

    private final String modelo;

    MotivoEntradaInvalida(String modelo) {
        this.modelo = modelo;
    }

    public String modelo() {
        return modelo;
    }
}
