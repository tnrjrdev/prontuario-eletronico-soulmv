package com.soulmv.hospitalar.mapper;

import com.soulmv.hospitalar.dto.response.UsuarioResponse;
import com.soulmv.hospitalar.entity.Usuario;
import com.soulmv.hospitalar.enums.Role;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-11T22:31:59-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class UsuarioMapperImpl implements UsuarioMapper {

    @Override
    public UsuarioResponse toResponse(Usuario usuario) {
        if ( usuario == null ) {
            return null;
        }

        Long id = null;
        String nomeCompleto = null;
        String login = null;
        String email = null;
        boolean ativo = false;
        Set<Role> roles = null;
        LocalDateTime criadoEm = null;
        LocalDateTime atualizadoEm = null;

        id = usuario.getId();
        nomeCompleto = usuario.getNomeCompleto();
        login = usuario.getLogin();
        email = usuario.getEmail();
        ativo = usuario.isAtivo();
        Set<Role> set = usuario.getRoles();
        if ( set != null ) {
            roles = new LinkedHashSet<Role>( set );
        }
        criadoEm = usuario.getCriadoEm();
        atualizadoEm = usuario.getAtualizadoEm();

        UsuarioResponse usuarioResponse = new UsuarioResponse( id, nomeCompleto, login, email, ativo, roles, criadoEm, atualizadoEm );

        return usuarioResponse;
    }
}
