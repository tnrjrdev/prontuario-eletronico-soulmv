package com.soulmv.catalogo.dto.request;

import com.soulmv.catalogo.enums.StatusLeito;
import jakarta.validation.constraints.NotNull;

public record LeitoStatusRequest(
        @NotNull(message = "O status é obrigatório")
        StatusLeito status
) {
}
