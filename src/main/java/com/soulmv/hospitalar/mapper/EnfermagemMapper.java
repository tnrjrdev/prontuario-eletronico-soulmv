package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.EvolucaoResponse;
import com.soulmv.hospitalar.dto.response.SinaisVitaisResponse;
import com.soulmv.hospitalar.dto.response.TriagemResponse;
import com.soulmv.hospitalar.entity.EvolucaoClinica;
import com.soulmv.hospitalar.entity.SinaisVitais;
import com.soulmv.hospitalar.entity.Triagem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnfermagemMapper {

    @Mapping(target = "atendimentoId", source = "atendimento.id")
    @Mapping(target = "descricaoRisco", expression = "java(triagem.getClassificacaoRisco().getDescricao())")
    @Mapping(target = "enfermeiroId", source = "enfermeiro.id")
    @Mapping(target = "enfermeiroNome", source = "enfermeiro.nomeCompleto")
    TriagemResponse toResponse(Triagem triagem);

    @Mapping(target = "atendimentoId", source = "atendimento.id")
    @Mapping(target = "registradoPorId", source = "registradoPor.id")
    @Mapping(target = "registradoPorNome", source = "registradoPor.nomeCompleto")
    SinaisVitaisResponse toResponse(SinaisVitais sinaisVitais);

    @Mapping(target = "atendimentoId", source = "atendimento.id")
    @Mapping(target = "autorId", source = "autor.id")
    @Mapping(target = "autorNome", source = "autor.nomeCompleto")
    EvolucaoResponse toResponse(EvolucaoClinica evolucao);
}
