package com.soulmv.catalogo.dto.response;

import java.util.Map;

public record LeitoEstatisticasResponse(
        long total,
        long ativos,
        long ocupados,
        long livres,
        Map<String, Long> porStatus
) {
}
