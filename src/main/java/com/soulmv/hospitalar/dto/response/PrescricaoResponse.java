package com.soulmv.hospitalar.dto.response;

import com.soulmv.hospitalar.enums.StatusPrescricao;

import java.time.LocalDateTime;
import java.util.List;

public record PrescricaoResponse(
        Long id,
        Long atendimentoId,
        Long pacienteId,
        String pacienteNome,
        Long medicoId,
        String medicoNome,
        StatusPrescricao status,
        String observacao,
        LocalDateTime dataHora,
        List<ItemPrescricaoResponse> itens
) {
}
