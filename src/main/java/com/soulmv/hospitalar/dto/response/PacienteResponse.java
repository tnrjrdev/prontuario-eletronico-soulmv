package com.soulmv.hospitalar.dto.response;

import com.soulmv.hospitalar.dto.request.EnderecoDto;
import com.soulmv.hospitalar.enums.Sexo;

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
        String convenioNome,
        String numeroCarteirinha,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
