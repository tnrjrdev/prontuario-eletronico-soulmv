package com.soulmv.catalogo.dto.response;

import com.soulmv.catalogo.enums.StatusLeito;

import java.time.LocalDateTime;

public record LeitoResponse(
        Long id,
        String identificador,
        Long setorId,
        String setorNome,
        StatusLeito status,
        boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
