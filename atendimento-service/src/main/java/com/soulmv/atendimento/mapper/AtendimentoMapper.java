package com.soulmv.atendimento.mapper;

import com.soulmv.atendimento.dto.response.AtendimentoResponse;
import com.soulmv.atendimento.entity.Atendimento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AtendimentoMapper {

    @Mapping(target = "profissionalId", source = "profissionalResponsavelId")
    @Mapping(target = "profissionalNome", source = "profissionalResponsavelNome")
    AtendimentoResponse toResponse(Atendimento atendimento);
}
