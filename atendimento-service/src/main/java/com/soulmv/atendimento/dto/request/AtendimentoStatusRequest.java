package com.soulmv.atendimento.dto.request;

import com.soulmv.atendimento.enums.StatusAtendimento;
import jakarta.validation.constraints.NotNull;

public record AtendimentoStatusRequest(
        @NotNull(message = "O status é obrigatório")
        StatusAtendimento status
) {
}
