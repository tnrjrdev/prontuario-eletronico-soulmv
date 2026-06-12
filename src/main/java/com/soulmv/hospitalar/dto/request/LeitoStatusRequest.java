package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.StatusLeito;
import jakarta.validation.constraints.NotNull;

public record LeitoStatusRequest(
        @NotNull(message = "O status é obrigatório")
        StatusLeito status
) {
}
