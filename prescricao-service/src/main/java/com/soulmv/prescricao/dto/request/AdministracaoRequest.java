package com.soulmv.prescricao.dto.request;

import com.soulmv.prescricao.enums.StatusAdministracao;
import jakarta.validation.constraints.NotNull;

public record AdministracaoRequest(@NotNull StatusAdministracao status, String observacao) {
}
