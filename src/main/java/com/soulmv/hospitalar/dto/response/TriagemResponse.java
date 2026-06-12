package com.soulmv.hospitalar.dto.response;

import com.soulmv.hospitalar.enums.ClassificacaoRisco;

import java.time.LocalDateTime;

public record TriagemResponse(
        Long id,
        Long atendimentoId,
        ClassificacaoRisco classificacaoRisco,
        String descricaoRisco,
        String observacao,
        Long enfermeiroId,
        String enfermeiroNome,
        LocalDateTime dataHora
) {
}
