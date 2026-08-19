package com.soulmv.exames.mapper;

import com.soulmv.exames.dto.response.ResultadoExameResponse;
import com.soulmv.exames.dto.response.SolicitacaoExameResponse;
import com.soulmv.exames.entity.ResultadoExame;
import com.soulmv.exames.entity.SolicitacaoExame;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExameMapper {

    SolicitacaoExameResponse toResponse(SolicitacaoExame solicitacao);

    @Mapping(target = "laudoAnexoId", source = "laudo.id")
    @Mapping(target = "temLaudo", expression = "java(resultado.getLaudo() != null)")
    ResultadoExameResponse toResponse(ResultadoExame resultado);
}
