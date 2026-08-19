package com.soulmv.atendimento.dto.request;

import jakarta.validation.constraints.NotNull;

public record AlocarLeitoRequest(
        @NotNull(message = "O leito é obrigatório")
        Long leitoId
) {
}
