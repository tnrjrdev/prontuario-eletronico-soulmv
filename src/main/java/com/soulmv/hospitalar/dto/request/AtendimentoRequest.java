package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.TipoAtendimento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Abertura de um atendimento (encontro) — normalmente feita pela recepção.
 */
public record AtendimentoRequest(
        @NotNull(message = "O paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "O tipo de atendimento é obrigatório")
        TipoAtendimento tipo,

        @NotNull(message = "O setor é obrigatório")
        Long setorId,

        @Size(max = 1000, message = "A queixa principal deve ter no máximo 1000 caracteres")
        String queixaPrincipal
) {
}
