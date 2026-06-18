package com.soulmv.auditoria.mapper;

import com.soulmv.auditoria.dto.response.AuditoriaResponse;
import com.soulmv.auditoria.entity.LogAuditoria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditoriaMapper {

    AuditoriaResponse toResponse(LogAuditoria log);
}
