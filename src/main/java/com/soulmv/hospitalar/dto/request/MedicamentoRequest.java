package com.soulmv.hospitalar.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MedicamentoRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        String principioAtivo,

        String concentracao,

        boolean controlado
) {
}
