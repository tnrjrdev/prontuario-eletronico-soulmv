package com.soulmv.atendimento.enums;

/**
 * Espelha catalogo-service StatusLeito — usada só pra montar o corpo da chamada Feign
 * de mudança de status do leito (o leito em si não é mais lido/gravado localmente).
 */
public enum StatusLeito {
    LIVRE,
    OCUPADO,
    MANUTENCAO,
    HIGIENIZACAO,
    INTERDITADO
}
