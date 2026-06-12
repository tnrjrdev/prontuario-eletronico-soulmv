package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.AuditoriaResponse;
import com.soulmv.hospitalar.entity.LogAuditoria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditoriaMapper {

    AuditoriaResponse toResponse(LogAuditoria log);
}
