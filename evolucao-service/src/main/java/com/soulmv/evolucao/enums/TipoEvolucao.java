package com.soulmv.evolucao.enums;

/**
 * Origem da evolução clínica. Regra: ENFERMEIRO só registra ENFERMAGEM;
 * MÉDICO só registra MEDICA — determinado pela role no JWT, não pelo cliente.
 */
public enum TipoEvolucao {
    MEDICA,
    ENFERMAGEM
}
