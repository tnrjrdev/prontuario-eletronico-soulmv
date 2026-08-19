package com.soulmv.prescricao.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PrescricaoRequest(
        String observacao,
        @NotEmpty @Valid List<ItemPrescricaoRequest> itens
) {
}
