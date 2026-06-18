package com.soulmv.auditoria.dto.response;

import java.time.LocalDateTime;

public record AuditoriaResponse(
        Long id,
        String usuarioLogin,
        String metodo,
        String caminho,
        int status,
        String ip,
        LocalDateTime dataHora
) {
}
