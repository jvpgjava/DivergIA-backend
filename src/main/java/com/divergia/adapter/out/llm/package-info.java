/**
 * Adaptador de saída para o LLM, implementado com LangChain4j.
 *
 * <p>Único pacote (junto com {@link com.divergia.adapter.out.vectorstore})
 * autorizado a importar tipos de LangChain4j. Implementa
 * {@code LlmPort} de {@link com.divergia.application.port.out} traduzindo
 * para/de tipos de domínio — nenhum tipo de LangChain4j escapa deste pacote.
 */
package com.divergia.adapter.out.llm;
