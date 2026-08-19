package com.soulmv.sinaisvitais.mapper;

import com.soulmv.sinaisvitais.dto.response.SinaisVitaisResponse;
import com.soulmv.sinaisvitais.entity.SinaisVitais;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SinaisVitaisMapper {

    SinaisVitaisResponse toResponse(SinaisVitais sinaisVitais);
}
