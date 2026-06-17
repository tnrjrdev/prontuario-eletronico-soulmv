package com.soulmv.iam.dto.request;

import com.soulmv.iam.enums.Role;
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
