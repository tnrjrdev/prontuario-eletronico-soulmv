package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.TipoConvenio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConvenioRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        String registroAns,

        @NotNull(message = "O tipo é obrigatório")
        TipoConvenio tipo
) {
}
