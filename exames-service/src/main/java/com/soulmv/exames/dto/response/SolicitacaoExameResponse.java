package com.soulmv.exames.dto.response;

import com.soulmv.exames.enums.StatusExame;

import java.time.LocalDateTime;

public record SolicitacaoExameResponse(
        Long id,
        Long atendimentoId,
        Long pacienteId,
        String pacienteNome,
        String tipoExame,
        StatusExame status,
        String observacao,
        Long medicoSolicitanteId,
        String medicoSolicitanteNome,
        LocalDateTime dataSolicitacao,
        ResultadoExameResponse resultado
) {
}
