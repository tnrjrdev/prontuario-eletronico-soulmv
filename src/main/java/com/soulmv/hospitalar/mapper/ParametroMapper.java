package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.Cid10Response;
import com.soulmv.hospitalar.dto.response.ConvenioResponse;
import com.soulmv.hospitalar.dto.response.LeitoResponse;
import com.soulmv.hospitalar.dto.response.MedicamentoResponse;
import com.soulmv.hospitalar.dto.response.ProcedimentoTussResponse;
import com.soulmv.hospitalar.dto.response.SetorResponse;
import com.soulmv.hospitalar.entity.Cid10;
import com.soulmv.hospitalar.entity.Convenio;
import com.soulmv.hospitalar.entity.Leito;
import com.soulmv.hospitalar.entity.Medicamento;
import com.soulmv.hospitalar.entity.ProcedimentoTuss;
import com.soulmv.hospitalar.entity.Setor;
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
