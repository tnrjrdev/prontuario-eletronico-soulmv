package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.StatusAdministracao;
import jakarta.validation.constraints.NotNull;

public record AdministracaoRequest(
        @NotNull(message = "O status da administração é obrigatório")
        StatusAdministracao status,

        String observacao
) {
}
