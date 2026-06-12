package com.soulmv.hospitalar.dto.response;

import com.soulmv.hospitalar.enums.StatusAtendimento;
import com.soulmv.hospitalar.enums.TipoAtendimento;

import java.time.LocalDateTime;

public record AtendimentoResponse(
        Long id,
        Long pacienteId,
        String pacienteNome,
        TipoAtendimento tipo,
        StatusAtendimento status,
        Long setorId,
        String setorNome,
        Long leitoId,
        String leitoIdentificador,
        Long profissionalId,
        String profissionalNome,
        String queixaPrincipal,
        LocalDateTime dataEntrada,
        LocalDateTime dataAlta,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
