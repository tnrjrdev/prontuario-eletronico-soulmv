package com.soulmv.hospitalar.dto.response;

import com.soulmv.hospitalar.enums.TipoDiagnostico;

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
