package com.soulmv.dashboard.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record FaturamentoDashboardResponse(
        long totalContas,
        BigDecimal valorTotalGeral,
        Map<String, Long> contasPorStatus,
        Map<String, BigDecimal> valorPorStatus
) {
}
