package com.soulmv.faturamento.dto.response;

import com.soulmv.faturamento.enums.StatusConta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ContaResponse(
        Long id,
        Long atendimentoId,
        Long pacienteId,
        String pacienteNome,
        Long convenioId,
        String convenioNome,
        StatusConta status,
        BigDecimal valorTotal,
        LocalDateTime dataFechamento,
        List<ItemContaResponse> itens,
        LocalDateTime criadoEm
) {
}
