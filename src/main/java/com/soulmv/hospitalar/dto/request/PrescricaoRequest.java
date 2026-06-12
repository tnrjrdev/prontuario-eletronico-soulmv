package com.soulmv.hospitalar.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PrescricaoRequest(
        String observacao,

        @NotEmpty(message = "Informe ao menos um item na prescrição")
        @Valid
        List<ItemPrescricaoRequest> itens
) {
}
