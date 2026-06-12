package com.soulmv.hospitalar.dto.response;

import com.soulmv.hospitalar.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Representação de saída de um usuário (nunca expõe a senha).
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
