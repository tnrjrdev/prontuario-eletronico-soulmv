package com.soulmv.triagem.dto.response;

import com.soulmv.triagem.enums.ClassificacaoRisco;

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
