package com.soulmv.atendimento.dto.response;

import com.soulmv.atendimento.enums.StatusAtendimento;
import com.soulmv.atendimento.enums.TipoAtendimento;

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
