package com.soulmv.triagem.mapper;

import com.soulmv.triagem.dto.response.TriagemResponse;
import com.soulmv.triagem.entity.Triagem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TriagemMapper {

    @Mapping(target = "descricaoRisco", expression = "java(triagem.getClassificacaoRisco().getDescricao())")
    TriagemResponse toResponse(Triagem triagem);
}
