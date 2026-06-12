package com.soulmv.hospitalar.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ItemContaRequest(
        @NotNull(message = "O procedimento é obrigatório")
        Long procedimentoId,

        @Positive(message = "A quantidade deve ser maior que zero")
        int quantidade,

        /** Opcional: se ausente, usa o valor de referência do procedimento. */
        @PositiveOrZero(message = "O valor unitário não pode ser negativo")
        BigDecimal valorUnitario
) {
}
