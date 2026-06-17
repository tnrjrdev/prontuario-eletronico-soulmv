package com.soulmv.paciente.mapper;

import com.soulmv.paciente.dto.request.EnderecoDto;
import com.soulmv.paciente.dto.response.PacienteResponse;
import com.soulmv.paciente.entity.Endereco;
import com.soulmv.paciente.entity.Paciente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Conversão entre Paciente e seus DTOs.
 */
@Mapper(componentModel = "spring")
public interface PacienteMapper {

    PacienteResponse toResponse(Paciente paciente);

    EnderecoDto toDto(Endereco endereco);

    Endereco toEntity(EnderecoDto dto);
}
