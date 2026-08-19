package com.soulmv.prescricao.dto.request;

import com.soulmv.prescricao.enums.ViaAdministracao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemPrescricaoRequest(
        @NotNull Long medicamentoId,
        @NotBlank String dose,
        ViaAdministracao via,
        String frequencia,
        String duracao,
        String observacao
) {
}
