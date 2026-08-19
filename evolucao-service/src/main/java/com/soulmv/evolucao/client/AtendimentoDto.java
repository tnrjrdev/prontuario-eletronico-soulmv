package com.soulmv.evolucao.client;

import com.soulmv.evolucao.enums.StatusAtendimento;

public record AtendimentoDto(Long id, StatusAtendimento status) {
}
