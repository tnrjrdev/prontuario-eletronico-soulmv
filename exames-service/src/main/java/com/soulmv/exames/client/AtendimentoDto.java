package com.soulmv.exames.client;

import com.soulmv.exames.enums.StatusAtendimento;

public record AtendimentoDto(Long id, StatusAtendimento status, Long pacienteId, String pacienteNome) {
}
