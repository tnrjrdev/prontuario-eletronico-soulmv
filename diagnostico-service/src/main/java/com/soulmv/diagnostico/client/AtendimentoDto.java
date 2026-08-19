package com.soulmv.diagnostico.client;

import com.soulmv.diagnostico.enums.StatusAtendimento;

public record AtendimentoDto(Long id, StatusAtendimento status) {
}
