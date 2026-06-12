package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.UsuarioResponse;
import com.soulmv.hospitalar.entity.Usuario;
import org.mapstruct.Mapper;

/**
 * Conversão entre a entidade Usuario e seus DTOs de saída.
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);
}
