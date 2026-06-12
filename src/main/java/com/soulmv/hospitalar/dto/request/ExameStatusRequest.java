package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.StatusExame;
import jakarta.validation.constraints.NotNull;

public record ExameStatusRequest(
        @NotNull(message = "O status é obrigatório")
        StatusExame status
) {
}
