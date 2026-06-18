package com.soulmv.agendamento.mapper;

import com.soulmv.agendamento.dto.response.AgendamentoResponse;
import com.soulmv.agendamento.entity.Agendamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgendamentoMapper {

    @Mapping(target = "pacienteId", source = "paciente.id")
    @Mapping(target = "pacienteNome", source = "paciente.nome")
    @Mapping(target = "profissionalId", source = "profissional.id")
    @Mapping(target = "profissionalNome", source = "profissional.nomeCompleto")
    @Mapping(target = "setorId", source = "setor.id")
    @Mapping(target = "setorNome", source = "setor.nome")
    @Mapping(target = "convenioId", source = "convenio.id")
    @Mapping(target = "convenioNome", source = "convenio.nome")
    @Mapping(target = "atendimentoId", source = "atendimento.id")
    AgendamentoResponse toResponse(Agendamento agendamento);
}
