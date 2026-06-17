package com.soulmv.iam.dto.response;

import com.soulmv.iam.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * RepresentaÃ§Ã£o de saÃ­da de um usuÃ¡rio (nunca expÃµe a senha).
 */
public record UsuarioResponse(
        Long id,
        String nomeCompleto,
        String login,
        String email,
        boolean ativo,
        Set<Role> roles,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
