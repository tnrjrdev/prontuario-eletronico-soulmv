package com.soulmv.agendamento.dto.request;

import com.soulmv.agendamento.enums.TipoAgendamento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Criação/reagendamento de um compromisso — normalmente feito pela recepção.
 */
public record AgendamentoRequest(
        @NotNull(message = "O paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "O profissional é obrigatório")
        Long profissionalId,

        @NotNull(message = "O setor é obrigatório")
        Long setorId,

        Long convenioId,

        @NotNull(message = "O tipo de agendamento é obrigatório")
        TipoAgendamento tipo,

        @NotNull(message = "A data/hora é obrigatória")
        @Future(message = "A data/hora deve ser no futuro")
        LocalDateTime dataHora,

        @Min(value = 5, message = "A duração mínima é de 5 minutos")
        @Max(value = 480, message = "A duração máxima é de 480 minutos")
        Integer duracaoMinutos,

        @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres")
        String observacoes
) {
}
