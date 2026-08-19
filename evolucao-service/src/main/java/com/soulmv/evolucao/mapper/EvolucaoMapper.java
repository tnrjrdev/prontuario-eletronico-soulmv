package com.soulmv.evolucao.mapper;

import com.soulmv.evolucao.dto.response.EvolucaoResponse;
import com.soulmv.evolucao.entity.EvolucaoClinica;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EvolucaoMapper {

    EvolucaoResponse toResponse(EvolucaoClinica evolucao);
}
