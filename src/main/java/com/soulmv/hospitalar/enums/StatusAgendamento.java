package com.soulmv.hospitalar.enums;

/**
 * Situação de um agendamento ao longo do seu ciclo de vida.
 */
public enum StatusAgendamento {
    /** Horário reservado, aguardando confirmação/comparecimento. */
    AGENDADO,
    /** Paciente confirmou presença. */
    CONFIRMADO,
    /** Check-in efetuado — gerou um atendimento (encontro). */
    REALIZADO,
    /** Cancelado pela recepção/paciente. */
    CANCELADO,
    /** Paciente não compareceu. */
    FALTOU;

    /** Estados finais: não admitem nova transição nem check-in. */
    public boolean isFinal() {
        return this == REALIZADO || this == CANCELADO || this == FALTOU;
    }

    /** Estados que ainda permitem check-in (conversão em atendimento). */
    public boolean permiteCheckin() {
        return this == AGENDADO || this == CONFIRMADO;
    }
}
