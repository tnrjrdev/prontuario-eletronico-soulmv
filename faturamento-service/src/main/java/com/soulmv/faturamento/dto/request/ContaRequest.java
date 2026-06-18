package com.soulmv.faturamento.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Abertura de conta hospitalar para um atendimento.
 */
public record ContaRequest(
        @NotNull(message = "O atendimento é obrigatório")
        Long atendimentoId
) {
}
