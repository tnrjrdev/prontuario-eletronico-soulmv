package com.soulmv.triagem.client;

import com.soulmv.triagem.enums.StatusAtendimento;

/** Projeção enxuta de AtendimentoResponse (atendimento-service) — só o que a triagem precisa. */
public record AtendimentoDto(Long id, StatusAtendimento status) {
}
