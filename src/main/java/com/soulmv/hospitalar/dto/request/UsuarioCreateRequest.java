package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Dados para criação de um usuário (operação restrita ao ADMIN).
 */
public record UsuarioCreateRequest(
        @NotBlank(message = "O nome completo é obrigatório")
        String nomeCompleto,

        @NotBlank(message = "O login é obrigatório")
        @Size(min = 3, max = 50, message = "O login deve ter entre 3 e 50 caracteres")
        String login,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 100, message = "A senha deve ter no mínimo 6 caracteres")
        String senha,

        @NotEmpty(message = "Informe ao menos um perfil")
        Set<Role> roles
) {
}
