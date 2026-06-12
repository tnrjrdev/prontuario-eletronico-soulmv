package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.StatusPrescricao;
import jakarta.validation.constraints.NotNull;

public record PrescricaoStatusRequest(
        @NotNull(message = "O status é obrigatório")
        StatusPrescricao status
) {
}
