package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.Role;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * Substitui o conjunto de perfis de um usuário.
 */
public record AtualizarRolesRequest(
        @NotEmpty(message = "Informe ao menos um perfil")
        Set<Role> roles
) {
}
