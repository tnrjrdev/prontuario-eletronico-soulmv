package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.AuditoriaResponse;
import com.soulmv.hospitalar.entity.LogAuditoria;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-11T22:31:59-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class AuditoriaMapperImpl implements AuditoriaMapper {

    @Override
    public AuditoriaResponse toResponse(LogAuditoria log) {
        if ( log == null ) {
            return null;
        }

        Long id = null;
        String usuarioLogin = null;
        String metodo = null;
        String caminho = null;
        int status = 0;
        String ip = null;
        LocalDateTime dataHora = null;

        id = log.getId();
        usuarioLogin = log.getUsuarioLogin();
        metodo = log.getMetodo();
        caminho = log.getCaminho();
        status = log.getStatus();
        ip = log.getIp();
        dataHora = log.getDataHora();

        AuditoriaResponse auditoriaResponse = new AuditoriaResponse( id, usuarioLogin, metodo, caminho, status, ip, dataHora );

        return auditoriaResponse;
    }
}
