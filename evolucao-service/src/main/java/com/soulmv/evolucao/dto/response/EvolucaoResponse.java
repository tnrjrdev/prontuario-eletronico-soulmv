package com.soulmv.evolucao.dto.response;

import com.soulmv.evolucao.enums.TipoEvolucao;

import java.time.LocalDateTime;

public record EvolucaoResponse(
        Long id,
        Long atendimentoId,
        TipoEvolucao tipo,
        String texto,
        Long autorId,
        String autorNome,
        LocalDateTime dataHora
) {
}
