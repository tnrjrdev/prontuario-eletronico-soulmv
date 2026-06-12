package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.TipoSetor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SetorRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "O tipo é obrigatório")
        TipoSetor tipo,

        String descricao
) {
}
