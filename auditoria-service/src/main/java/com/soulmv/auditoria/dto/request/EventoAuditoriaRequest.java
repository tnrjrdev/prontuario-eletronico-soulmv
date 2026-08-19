package com.soulmv.auditoria.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record EventoAuditoriaRequest(
        @NotBlank String usuarioLogin,
        @NotBlank String metodo,
        @NotBlank String caminho,
        @Min(100) int status,
        String ip
) {
}
