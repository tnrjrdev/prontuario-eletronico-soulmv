package com.soulmv.hospitalar.dto.response;

import java.time.LocalDateTime;

public record AnamneseResponse(
        Long id,
        Long atendimentoId,
        Long medicoId,
        String medicoNome,
        String historiaDoencaAtual,
        String antecedentes,
        String alergias,
        String exameFisico,
        LocalDateTime dataHora
) {
}
