package com.soulmv.catalogo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProcedimentoTussRequest(
        @NotBlank(message = "O código TUSS é obrigatório")
        String codigoTuss,

        @NotBlank(message = "A descrição é obrigatória")
        String descricao,

        @PositiveOrZero(message = "O valor de referência não pode ser negativo")
        BigDecimal valorReferencia
) {
}
