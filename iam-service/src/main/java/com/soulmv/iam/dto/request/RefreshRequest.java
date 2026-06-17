package com.soulmv.iam.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * SolicitaÃ§Ã£o de renovaÃ§Ã£o de access token a partir de um refresh token vÃ¡lido.
 */
public record RefreshRequest(
        @NotBlank(message = "O refreshToken Ã© obrigatÃ³rio")
        String refreshToken
) {
}
