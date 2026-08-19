package com.soulmv.exames.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SolicitacaoExameRequest(@NotBlank String tipoExame, String observacao) {
}
