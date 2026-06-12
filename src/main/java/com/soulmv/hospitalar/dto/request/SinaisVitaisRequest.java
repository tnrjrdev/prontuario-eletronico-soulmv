package com.soulmv.hospitalar.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record SinaisVitaisRequest(
        @PositiveOrZero(message = "Pressão sistólica inválida")
        Integer pressaoSistolica,

        @PositiveOrZero(message = "Pressão diastólica inválida")
        Integer pressaoDiastolica,

        @PositiveOrZero(message = "Frequência cardíaca inválida")
        Integer frequenciaCardiaca,

        @PositiveOrZero(message = "Frequência respiratória inválida")
        Integer frequenciaRespiratoria,

        @PositiveOrZero(message = "Temperatura inválida")
        BigDecimal temperatura,

        @Min(value = 0, message = "Saturação inválida")
        @Max(value = 100, message = "Saturação inválida")
        Integer saturacaoO2,

        @PositiveOrZero(message = "Glicemia inválida")
        Integer glicemia,

        @Min(value = 0, message = "A escala de dor vai de 0 a 10")
        @Max(value = 10, message = "A escala de dor vai de 0 a 10")
        Integer escalaDor
) {
}
