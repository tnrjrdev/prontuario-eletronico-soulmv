package com.soulmv.agendamento.dto.request;

import com.soulmv.agendamento.enums.StatusAgendamento;
import jakarta.validation.constraints.NotNull;

/**
 * Transição de status do agendamento (CONFIRMADO / CANCELADO / FALTOU).
 * O status REALIZADO só é atingido via check-in.
 */
public record AgendamentoStatusRequest(
        @NotNull(message = "O status é obrigatório")
        StatusAgendamento status
) {
}
