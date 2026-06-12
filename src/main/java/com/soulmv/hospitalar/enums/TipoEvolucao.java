package com.soulmv.hospitalar.enums;

/**
 * Origem da evolução clínica. Regra: ENFERMEIRO só registra ENFERMAGEM;
 * MÉDICO só registra MEDICA. Ninguém edita evolução de outra origem.
 */
public enum TipoEvolucao {
    MEDICA,
    ENFERMAGEM
}
