package com.soulmv.hospitalar.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Alocação de leito a um atendimento (internação).
 */
public record AlocarLeitoRequest(
        @NotNull(message = "O leito é obrigatório")
        Long leitoId
) {
}
