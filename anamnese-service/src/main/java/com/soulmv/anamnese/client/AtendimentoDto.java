package com.soulmv.anamnese.client;

import com.soulmv.anamnese.enums.StatusAtendimento;

public record AtendimentoDto(Long id, StatusAtendimento status) {
}
