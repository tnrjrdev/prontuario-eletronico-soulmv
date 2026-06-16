package com.soulmv.hospitalar.dto.response;

import com.soulmv.hospitalar.enums.StatusAgendamento;
import com.soulmv.hospitalar.enums.TipoAgendamento;

import java.time.LocalDateTime;

public record AgendamentoResponse(
        Long id,
        Long pacienteId,
        String pacienteNome,
        Long profissionalId,
        String profissionalNome,
        Long setorId,
        String setorNome,
        Long convenioId,
        String convenioNome,
        TipoAgendamento tipo,
        StatusAgendamento status,
        LocalDateTime dataHora,
        Integer duracaoMinutos,
        String observacoes,
        Long atendimentoId,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
