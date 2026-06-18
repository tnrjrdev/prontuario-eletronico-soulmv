package com.soulmv.dashboard.dto.response;

import java.util.Map;

public record OcupacaoLeitosResponse(
        long totalLeitos,
        long leitosAtivos,
        long ocupados,
        long livres,
        double taxaOcupacaoPercent,
        Map<String, Long> porStatus
) {
}
