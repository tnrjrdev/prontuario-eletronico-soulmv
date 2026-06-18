package com.soulmv.dashboard.dto.response;

import java.util.Map;

public record AtendimentosDashboardResponse(
        long total,
        Map<String, Long> porStatus
) {
}
