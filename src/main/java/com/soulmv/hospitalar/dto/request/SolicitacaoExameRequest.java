package com.soulmv.hospitalar.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SolicitacaoExameRequest(
        @NotBlank(message = "O tipo de exame é obrigatório")
        String tipoExame,

        String observacao
) {
}
