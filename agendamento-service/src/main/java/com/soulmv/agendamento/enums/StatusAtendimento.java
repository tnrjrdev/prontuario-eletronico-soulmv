package com.soulmv.agendamento.enums;

/**
 * Situação do atendimento ao longo do fluxo assistencial.
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

    /** Estados finais: não admitem nova transição de status. */
    public boolean isFinal() {
        return this == ALTA || this == CANCELADO;
    }
}
