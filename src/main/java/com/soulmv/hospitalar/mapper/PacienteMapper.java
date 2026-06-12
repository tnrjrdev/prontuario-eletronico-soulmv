package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.request.EnderecoDto;
import com.soulmv.hospitalar.dto.response.PacienteResponse;
import com.soulmv.hospitalar.entity.Endereco;
import com.soulmv.hospitalar.entity.Paciente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Conversão entre Paciente e seus DTOs.
 */
@Mapper(componentModel = "spring")
public interface PacienteMapper {

    @Mapping(target = "convenioId", source = "convenio.id")
    @Mapping(target = "convenioNome", source = "convenio.nome")
    PacienteResponse toResponse(Paciente paciente);

    EnderecoDto toDto(Endereco endereco);

    Endereco toEntity(EnderecoDto dto);
}
