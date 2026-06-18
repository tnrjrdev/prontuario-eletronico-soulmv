package com.soulmv.catalogo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LeitoRequest(
        @NotBlank(message = "O identificador é obrigatório")
        String identificador,

        @NotNull(message = "O setor é obrigatório")
        Long setorId
) {
}
