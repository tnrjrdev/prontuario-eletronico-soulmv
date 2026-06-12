package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.TipoDiagnostico;
import jakarta.validation.constraints.NotNull;

public record DiagnosticoRequest(
        @NotNull(message = "A CID-10 é obrigatória")
        Long cid10Id,

        @NotNull(message = "O tipo do diagnóstico é obrigatório")
        TipoDiagnostico tipo,

        String observacao
) {
}
