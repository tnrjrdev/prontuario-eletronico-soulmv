package com.soulmv.prescricao.dto.response;

import com.soulmv.prescricao.enums.StatusAdministracao;

import java.time.LocalDateTime;

public record AdministracaoResponse(
        Long id,
        Long itemPrescricaoId,
        String medicamentoNome,
        Long enfermeiroId,
        String enfermeiroNome,
        StatusAdministracao status,
        LocalDateTime dataHoraAdministracao,
        String observacao
) {
}
