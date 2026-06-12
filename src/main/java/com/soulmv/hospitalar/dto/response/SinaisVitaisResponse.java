package com.soulmv.hospitalar.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SinaisVitaisResponse(
        Long id,
        Long atendimentoId,
        Integer pressaoSistolica,
        Integer pressaoDiastolica,
        Integer frequenciaCardiaca,
        Integer frequenciaRespiratoria,
        BigDecimal temperatura,
        Integer saturacaoO2,
        Integer glicemia,
        Integer escalaDor,
        Long registradoPorId,
        String registradoPorNome,
        LocalDateTime dataHora
) {
}
