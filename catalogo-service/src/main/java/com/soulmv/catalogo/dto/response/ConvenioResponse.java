package com.soulmv.catalogo.dto.response;

import com.soulmv.catalogo.enums.TipoConvenio;

import java.time.LocalDateTime;

public record ConvenioResponse(
        Long id,
        String nome,
        String registroAns,
        TipoConvenio tipo,
        boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
