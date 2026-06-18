package com.soulmv.catalogo.mapper;

import com.soulmv.catalogo.dto.response.Cid10Response;
import com.soulmv.catalogo.dto.response.ConvenioResponse;
import com.soulmv.catalogo.dto.response.LeitoResponse;
import com.soulmv.catalogo.dto.response.MedicamentoResponse;
import com.soulmv.catalogo.dto.response.ProcedimentoTussResponse;
import com.soulmv.catalogo.dto.response.SetorResponse;
import com.soulmv.catalogo.entity.Cid10;
import com.soulmv.catalogo.entity.Convenio;
import com.soulmv.catalogo.entity.Leito;
import com.soulmv.catalogo.entity.Medicamento;
import com.soulmv.catalogo.entity.ProcedimentoTuss;
import com.soulmv.catalogo.entity.Setor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Conversões entidade → DTO de resposta para os cadastros/parâmetros (Etapa 3).
 */
@Mapper(componentModel = "spring")
public interface ParametroMapper {

    SetorResponse toResponse(Setor setor);

    @Mapping(target = "setorId", source = "setor.id")
    @Mapping(target = "setorNome", source = "setor.nome")
    LeitoResponse toResponse(Leito leito);

    ConvenioResponse toResponse(Convenio convenio);

    MedicamentoResponse toResponse(Medicamento medicamento);

    ProcedimentoTussResponse toResponse(ProcedimentoTuss procedimento);

    Cid10Response toResponse(Cid10 cid10);
}
