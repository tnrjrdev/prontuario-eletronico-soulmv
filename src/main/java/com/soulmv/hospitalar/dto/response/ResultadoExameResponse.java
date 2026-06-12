package com.soulmv.hospitalar.dto.response;

import java.time.LocalDateTime;

public record ResultadoExameResponse(
        Long id,
        String resultadoTexto,
        Long laudoAnexoId,
        boolean temLaudo,
        Long liberadoPorId,
        String liberadoPorNome,
        LocalDateTime dataLiberacao
) {
}
