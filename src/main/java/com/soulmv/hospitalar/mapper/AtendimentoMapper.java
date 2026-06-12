package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.AtendimentoResponse;
import com.soulmv.hospitalar.entity.Atendimento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AtendimentoMapper {

    @Mapping(target = "pacienteId", source = "paciente.id")
    @Mapping(target = "pacienteNome", source = "paciente.nome")
    @Mapping(target = "setorId", source = "setor.id")
    @Mapping(target = "setorNome", source = "setor.nome")
    @Mapping(target = "leitoId", source = "leito.id")
    @Mapping(target = "leitoIdentificador", source = "leito.identificador")
    @Mapping(target = "profissionalId", source = "profissionalResponsavel.id")
    @Mapping(target = "profissionalNome", source = "profissionalResponsavel.nomeCompleto")
    AtendimentoResponse toResponse(Atendimento atendimento);
}
