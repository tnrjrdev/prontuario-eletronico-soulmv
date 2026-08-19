package com.soulmv.agendamento.mapper;

import com.soulmv.agendamento.dto.response.AgendamentoResponse;
import com.soulmv.agendamento.entity.Agendamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgendamentoMapper {

    @Mapping(target = "atendimentoId", source = "atendimento.id")
    AgendamentoResponse toResponse(Agendamento agendamento);
}
