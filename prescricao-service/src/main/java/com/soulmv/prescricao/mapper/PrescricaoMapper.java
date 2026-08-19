package com.soulmv.prescricao.mapper;

import com.soulmv.prescricao.dto.response.ItemPrescricaoResponse;
import com.soulmv.prescricao.dto.response.PrescricaoResponse;
import com.soulmv.prescricao.entity.ItemPrescricao;
import com.soulmv.prescricao.entity.Prescricao;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PrescricaoMapper {

    PrescricaoResponse toResponse(Prescricao prescricao);

    ItemPrescricaoResponse toResponse(ItemPrescricao item);
}
