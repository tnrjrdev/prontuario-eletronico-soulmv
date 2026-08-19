package com.soulmv.sinaisvitais.client;

import com.soulmv.sinaisvitais.enums.StatusAtendimento;

public record AtendimentoDto(Long id, StatusAtendimento status) {
}
