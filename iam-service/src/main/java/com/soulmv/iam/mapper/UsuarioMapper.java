package com.soulmv.iam.mapper;

import com.soulmv.iam.dto.response.UsuarioResponse;
import com.soulmv.iam.entity.Usuario;
import org.mapstruct.Mapper;

/**
 * ConversÃ£o entre a entidade Usuario e seus DTOs de saÃ­da.
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);
}
