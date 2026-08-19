package com.soulmv.faturamento.client;

import java.math.BigDecimal;

/** Espelha catalogo-service ProcedimentoTussResponse. */
public record ProcedimentoTussDto(
        Long id,
        String codigoTuss,
        String descricao,
        BigDecimal valorReferencia,
        boolean ativo
) {
}
