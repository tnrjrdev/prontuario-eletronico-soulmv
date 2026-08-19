package com.soulmv.anamnese.enums;

/**
 * Espelha atendimento-service StatusAtendimento — usada só pra interpretar a resposta
 * do Feign (Atendimento em si não é lido/gravado localmente).
 */
public enum StatusAtendimento {
    AGUARDANDO_TRIAGEM,
    EM_TRIAGEM,
    AGUARDANDO_ATENDIMENTO,
    EM_ATENDIMENTO,
    INTERNADO,
    AGUARDANDO_EXAME,
    ALTA,
    CANCELADO;

    public boolean isFinal() {
        return this == ALTA || this == CANCELADO;
    }
}
