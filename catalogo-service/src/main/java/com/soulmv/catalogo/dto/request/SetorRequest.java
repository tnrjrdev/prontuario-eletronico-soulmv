package com.soulmv.catalogo.dto.request;

import com.soulmv.catalogo.enums.TipoSetor;
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
