package com.soulmv.prescricao.client;

import com.soulmv.prescricao.enums.StatusAtendimento;

public record AtendimentoDto(Long id, StatusAtendimento status, Long pacienteId, String pacienteNome) {
}
