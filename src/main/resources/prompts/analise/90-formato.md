# Formato da resposta

Responda EXCLUSIVAMENTE com um objeto JSON válido, sem markdown, sem cercas de código e sem texto antes ou depois, exatamente com estas chaves:

{"triagem":{"status":"VALIDO","campo":"AMBOS","idiomaOriginal":"pt","idiomaEditado":"pt","confianca":0.95},"derivas":[{"tipoDesvio":"SENTIDO","trechoOriginal":"...","trechoEditado":"...","explicacao":"...","intensidade":0.5}]}

- "status": VALIDO, NAO_E_TEXTO, CONTEUDO_CODIFICADO, TEXTO_SEM_SENTIDO, INSTRUCAO_PARA_IA, SO_OFENSA, IDIOMAS_DIFERENTES ou SEM_RELACAO.
- "campo": ORIGINAL, EDITADO ou AMBOS.
- "tipoDesvio": SENTIDO, POSICAO ou INTENSIDADE (sem acento).
- "intensidade": número de 0.0 a 1.0.
- Sem nenhuma deriva: "derivas":[].
