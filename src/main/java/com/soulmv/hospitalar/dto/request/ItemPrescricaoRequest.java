package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.ViaAdministracao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemPrescricaoRequest(
        @NotNull(message = "O medicamento é obrigatório")
        Long medicamentoId,

        @NotBlank(message = "A dose é obrigatória")
        String dose,

        ViaAdministracao via,

        String frequencia,

        String duracao,

        String observacao
) {
}
