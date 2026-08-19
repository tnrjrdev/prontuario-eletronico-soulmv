package com.soulmv.anamnese.mapper;

import com.soulmv.anamnese.dto.response.AnamneseResponse;
import com.soulmv.anamnese.entity.Anamnese;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnamneseMapper {

    AnamneseResponse toResponse(Anamnese anamnese);
}
