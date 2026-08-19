package com.soulmv.prescricao.dto.response;

import com.soulmv.prescricao.enums.ViaAdministracao;

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
