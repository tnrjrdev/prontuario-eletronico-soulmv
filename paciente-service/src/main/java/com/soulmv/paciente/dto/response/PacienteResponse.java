package com.soulmv.paciente.dto.response;

import com.soulmv.paciente.dto.request.EnderecoDto;
import com.soulmv.paciente.enums.Sexo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponse(
        Long id,
        String nome,
        String cpf,
        String cartaoSus,
        LocalDate dataNascimento,
        Sexo sexo,
        String telefone,
        String email,
        EnderecoDto endereco,
        Long convenioId,
        String numeroCarteirinha,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
