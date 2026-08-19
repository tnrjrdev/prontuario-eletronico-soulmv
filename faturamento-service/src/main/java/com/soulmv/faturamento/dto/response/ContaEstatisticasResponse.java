package com.soulmv.faturamento.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record ContaEstatisticasResponse(
        long total,
        BigDecimal valorTotal,
        Map<String, Long> contasPorStatus,
        Map<String, BigDecimal> valorPorStatus
) {
}
