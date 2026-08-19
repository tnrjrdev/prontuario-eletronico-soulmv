package com.soulmv.dashboard.client;

import java.math.BigDecimal;
import java.util.Map;

/** Espelha faturamento-service ContaEstatisticasResponse. */
public record ContaEstatisticasDto(
        long total,
        BigDecimal valorTotal,
        Map<String, Long> contasPorStatus,
        Map<String, BigDecimal> valorPorStatus
) {
}
