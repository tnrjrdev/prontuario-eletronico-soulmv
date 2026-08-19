package com.soulmv.triagem.dto.request;

import com.soulmv.triagem.enums.ClassificacaoRisco;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TriagemRequest(
        @NotNull(message = "A classificação de risco é obrigatória")
        ClassificacaoRisco classificacaoRisco,

        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
        String observacao
) {
}
