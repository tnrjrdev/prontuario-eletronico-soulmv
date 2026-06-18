package com.soulmv.catalogo.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Ativa ou inativa um usuário.
 */
public record AtualizarStatusRequest(
        @NotNull(message = "Informe o status (ativo)")
        Boolean ativo
) {
}
