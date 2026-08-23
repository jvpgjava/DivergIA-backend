/**
 * Adaptador de saída para persistência via Spring Data JPA.
 *
 * <p>Contém entidades JPA equivalentes às de {@link com.divergia.domain.model},
 * com mapper explícito entre as duas — a entidade de domínio nunca é
 * anotada com {@code @Entity} diretamente.
 */
package com.divergia.adapter.out.persistence;
