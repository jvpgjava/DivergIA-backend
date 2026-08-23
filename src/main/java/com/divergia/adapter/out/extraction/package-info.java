/**
 * Adaptador de saída para o microsserviço Python de extração de documento (Docling).
 *
 * <p>Único pacote autorizado a conhecer o contrato HTTP desse serviço.
 * Implementa {@code ExtracaoDocumentoPort} de
 * {@link com.divergia.application.port.out}, chamando o serviço via
 * cliente HTTP com timeout definido.
 */
package com.divergia.adapter.out.extraction;
