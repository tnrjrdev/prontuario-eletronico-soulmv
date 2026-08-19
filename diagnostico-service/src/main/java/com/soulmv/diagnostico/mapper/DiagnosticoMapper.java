package com.soulmv.diagnostico.mapper;

import com.soulmv.diagnostico.dto.response.DiagnosticoResponse;
import com.soulmv.diagnostico.entity.Diagnostico;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DiagnosticoMapper {

    DiagnosticoResponse toResponse(Diagnostico diagnostico);
}
