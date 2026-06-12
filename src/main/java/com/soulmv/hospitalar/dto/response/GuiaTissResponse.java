package com.soulmv.hospitalar.dto.response;

import com.soulmv.hospitalar.enums.StatusGuiaTiss;

import java.time.LocalDateTime;

public record GuiaTissResponse(
        Long id,
        Long contaId,
        String numeroGuia,
        StatusGuiaTiss status,
        LocalDateTime dataGeracao
) {
}
