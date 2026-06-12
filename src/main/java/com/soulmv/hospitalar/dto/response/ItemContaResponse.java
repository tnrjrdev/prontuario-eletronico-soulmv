package com.soulmv.hospitalar.dto.response;

import java.math.BigDecimal;

public record ItemContaResponse(
        Long id,
        Long procedimentoId,
        String codigoTuss,
        String descricao,
        int quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal
) {
}
