package com.soulmv.diagnostico.dto.response;

import com.soulmv.diagnostico.enums.TipoDiagnostico;

import java.time.LocalDateTime;

public record DiagnosticoResponse(
        Long id,
        Long atendimentoId,
        Long cid10Id,
        String cid10Codigo,
        String cid10Descricao,
        TipoDiagnostico tipo,
        Long medicoId,
        String medicoNome,
        String observacao,
        LocalDateTime dataHora
) {
}
