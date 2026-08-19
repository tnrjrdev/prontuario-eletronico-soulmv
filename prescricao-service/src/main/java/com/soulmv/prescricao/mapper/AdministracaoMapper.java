package com.soulmv.prescricao.mapper;

import com.soulmv.prescricao.dto.response.AdministracaoResponse;
import com.soulmv.prescricao.entity.AdministracaoMedicamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdministracaoMapper {

    @Mapping(target = "itemPrescricaoId", source = "itemPrescricao.id")
    @Mapping(target = "medicamentoNome", source = "itemPrescricao.medicamentoNome")
    AdministracaoResponse toResponse(AdministracaoMedicamento administracao);
}
