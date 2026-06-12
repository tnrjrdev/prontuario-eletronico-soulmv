package com.soulmv.hospitalar.dto.response;

import com.soulmv.hospitalar.enums.ViaAdministracao;

public record ItemPrescricaoResponse(
        Long id,
        Long medicamentoId,
        String medicamentoNome,
        boolean medicamentoControlado,
        String dose,
        ViaAdministracao via,
        String frequencia,
        String duracao,
        String observacao
) {
}
