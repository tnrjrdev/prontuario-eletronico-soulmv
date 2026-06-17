package com.soulmv.iam.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciais de login.
 */
public record LoginRequest(
        @NotBlank(message = "O login Ã© obrigatÃ³rio")
        String login,

        @NotBlank(message = "A senha Ã© obrigatÃ³ria")
        String senha
) {
}
