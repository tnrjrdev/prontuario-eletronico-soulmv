package com.soulmv.faturamento.dto.response;

import com.soulmv.faturamento.enums.StatusGuiaTiss;

import java.time.LocalDateTime;

public record GuiaTissResponse(
        Long id,
        Long contaId,
        String numeroGuia,
        StatusGuiaTiss status,
        LocalDateTime dataGeracao
) {
}
