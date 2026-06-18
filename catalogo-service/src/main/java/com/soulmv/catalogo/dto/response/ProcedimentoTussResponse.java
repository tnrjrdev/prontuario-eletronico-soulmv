package com.soulmv.catalogo.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProcedimentoTussResponse(
        Long id,
        String codigoTuss,
        String descricao,
        BigDecimal valorReferencia,
        boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
