package com.soulmv.catalogo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record Cid10Request(
        @NotBlank(message = "O código é obrigatório")
        String codigo,

        @NotBlank(message = "A descrição é obrigatória")
        String descricao
) {
}
