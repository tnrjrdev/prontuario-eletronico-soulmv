package com.soulmv.dashboard.client;

import java.util.Map;

/** Espelha catalogo-service LeitoEstatisticasResponse. */
public record LeitoEstatisticasDto(
        long total,
        long ativos,
        long ocupados,
        long livres,
        Map<String, Long> porStatus
) {
}
