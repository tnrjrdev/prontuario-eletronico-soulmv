package com.soulmv.hospitalar.dto.response;

import java.time.LocalDateTime;

public record MedicamentoResponse(
        Long id,
        String nome,
        String principioAtivo,
        String concentracao,
        boolean controlado,
        boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
