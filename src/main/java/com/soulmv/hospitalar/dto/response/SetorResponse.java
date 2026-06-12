package com.soulmv.hospitalar.dto.response;

import com.soulmv.hospitalar.enums.TipoSetor;

import java.time.LocalDateTime;

public record SetorResponse(
        Long id,
        String nome,
        TipoSetor tipo,
        String descricao,
        boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
