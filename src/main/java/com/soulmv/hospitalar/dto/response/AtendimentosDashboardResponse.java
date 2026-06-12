package com.soulmv.hospitalar.dto.response;

import java.util.Map;

public record AtendimentosDashboardResponse(
        long total,
        Map<String, Long> porStatus
) {
}
