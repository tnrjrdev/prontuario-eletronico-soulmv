package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.StatusAtendimento;
import jakarta.validation.constraints.NotNull;

public record AtendimentoStatusRequest(
        @NotNull(message = "O status é obrigatório")
        StatusAtendimento status
) {
}
