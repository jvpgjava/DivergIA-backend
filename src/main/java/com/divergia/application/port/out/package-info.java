/**
 * Portas de saída — o que o domínio precisa do mundo externo (LLM, base
 * vetorial, extração de documento, persistência).
 *
 * <p>Interfaces implementadas por {@link com.divergia.adapter.out}. Nenhum
 * tipo de framework de infraestrutura (LangChain4j, JPA, cliente HTTP)
 * aparece nas assinaturas aqui — apenas tipos de domínio.
 */
package com.divergia.application.port.out;
