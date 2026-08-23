/**
 * Implementação dos casos de uso do DivergIA.
 *
 * <p>Cada classe implementa uma porta de {@link com.divergia.application.port.in}
 * e depende apenas de portas de {@link com.divergia.application.port.out} —
 * nunca de uma classe concreta de {@link com.divergia.adapter.out}. Um caso
 * de uso faz uma coisa; orquestração que cresce demais deve extrair
 * colaboradores em vez de acumular responsabilidade numa única classe.
 */
package com.divergia.application.usecase;
